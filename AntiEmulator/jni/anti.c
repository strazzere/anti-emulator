/*
 * Fun with qemu arm issues
 *
 * <diff@protonmail.com>
 */

#include <jni.h>
#include <signal.h> // sigtrap stuff, duh
#include <stdlib.h>
#include <sys/wait.h> // for waitpid
#include <unistd.h> // fork() / sleep()

// What qemuBkpt() returns
#define RESULT_UNKNOWN -1        // fork or waitpid failed, no evidence either way
#define RESULT_NOT_APPLICABLE -2 // not 32 bit arm, so there is no breakpoint to test (and nothing was forked)
#define RESULT_REAL_DEVICE 0     // the breakpoint raised a signal, as it does on hardware
#define RESULT_FROZEN 1          // the child never finished
#define RESULT_CRASHED 2         // the child died of something other than our handler
#define RESULT_IGNORED 3         // the child ran straight past the breakpoint

// Exit code of the child when its signal handler ran, i.e. the breakpoint was delivered
#define TRAPPED_EXIT_CODE 255

#if defined(__arm__)

// These run in the forked child, which is a copy of a multi threaded VM that has only this one thread. So they
// must only call async-signal-safe functions, and they must never return into (or exit through) the VM: _exit()
// instead of exit(), which would run the atexit handlers of a process that is in an unusable state.
void handler_sigtrap(int signo) {
  _exit(TRAPPED_EXIT_CODE);
}

void handler_sigbus(int signo) {
  _exit(TRAPPED_EXIT_CODE);
}

void setupSigTrap() {
  // BKPT throws SIGTRAP on nexus 5 / oneplus one (and most devices)
  signal(SIGTRAP, handler_sigtrap);
  // BKPT throws SIGBUS on nexus 4
  signal(SIGBUS, handler_sigbus);
}

// This will cause a SIGSEGV on some QEMU or be properly respected
void tryBKPT() {
  __asm__ __volatile__ ("bkpt 255");
}

#endif

jint Java_diff_strazzere_anti_emulator_FindEmulator_qemuBkpt(JNIEnv* env, jobject jObject) {
#if !defined(__arm__)
  // BKPT is a 32 bit arm instruction. Don't fork at all elsewhere: a forked child of the VM that is not killed by
  // the breakpoint would carry on running the app (a second, broken copy of it) once this function returned.
  return RESULT_NOT_APPLICABLE;
#else
  pid_t child = fork();

  if (child == -1) {
    return RESULT_UNKNOWN;
  }

  if (child == 0) {
    setupSigTrap();
    tryBKPT();

    // Only reached if the breakpoint was ignored. Whatever happens, the child must not return to the caller.
    _exit(0);
  }

  int child_status = 0;
  int finished = 0;
  int failed = 0;

  // Time could be adjusted here, though in my experience if the child has not returned instantly
  // then something has gone wrong and it is an emulated device
  for (int attempt = 0; attempt < 3; attempt++) {
    pid_t result = waitpid(child, &child_status, WNOHANG);
    if (result == child) {
      finished = 1;
      break;
    }
    if (result == -1) {
      // Can't tell, e.g. SIGCHLD is ignored and the child was already reaped for us
      failed = 1;
      break;
    }
    if (attempt < 2) {
      sleep(1);
    }
  }

  int status;
  if (finished) {
    if (WIFEXITED(child_status)) {
      // Our handler ran (likely a real device) or the breakpoint was ignored
      status = (WEXITSTATUS(child_status) == TRAPPED_EXIT_CODE) ? RESULT_REAL_DEVICE : RESULT_IGNORED;
    } else {
      // Didn't exit properly - very likely an emulator
      status = RESULT_CRASHED;
    }
  } else if (failed) {
    status = RESULT_UNKNOWN;
  } else {
    // Process timed out - likely an emulated device and child is frozen
    status = RESULT_FROZEN;
  }

  if (!finished) {
    // Ensure child is dead, and reap it so it doesn't stay around as a zombie
    kill(child, SIGKILL);
    waitpid(child, &child_status, 0);
  }

  return status;
#endif
}
