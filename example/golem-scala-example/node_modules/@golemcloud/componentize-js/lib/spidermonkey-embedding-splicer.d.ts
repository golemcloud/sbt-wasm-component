/**
* # Variants
* 
* ## `"i32"`
* 
* ## `"i64"`
* 
* ## `"f32"`
* 
* ## `"f64"`
*/
export type CoreTy = 'i32' | 'i64' | 'f32' | 'f64';
/**
* # Variants
* 
* ## `"stdio"`
* 
* ## `"clocks"`
* 
* ## `"random"`
* 
* ## `"http"`
*/
export type Features = 'stdio' | 'clocks' | 'random' | 'http';
export interface CoreFn {
  params: Array<CoreTy>,
  ret?: CoreTy,
  retptr: boolean,
  retsize: number,
  paramptr: boolean,
}
export interface SpliceResult {
  wasm: Uint8Array,
  jsBindings: string,
  exports: Array<[string, CoreFn]>,
  importWrappers: Array<[string, string]>,
  imports: Array<[string, string, number]>,
}
import { WasiCliEnvironment } from './interfaces/wasi-cli-environment.js';
import { WasiCliExit } from './interfaces/wasi-cli-exit.js';
import { WasiCliStderr } from './interfaces/wasi-cli-stderr.js';
import { WasiCliStdin } from './interfaces/wasi-cli-stdin.js';
import { WasiCliStdout } from './interfaces/wasi-cli-stdout.js';
import { WasiCliTerminalInput } from './interfaces/wasi-cli-terminal-input.js';
import { WasiCliTerminalOutput } from './interfaces/wasi-cli-terminal-output.js';
import { WasiCliTerminalStderr } from './interfaces/wasi-cli-terminal-stderr.js';
import { WasiCliTerminalStdin } from './interfaces/wasi-cli-terminal-stdin.js';
import { WasiCliTerminalStdout } from './interfaces/wasi-cli-terminal-stdout.js';
import { WasiClocksMonotonicClock } from './interfaces/wasi-clocks-monotonic-clock.js';
import { WasiClocksWallClock } from './interfaces/wasi-clocks-wall-clock.js';
import { WasiFilesystemPreopens } from './interfaces/wasi-filesystem-preopens.js';
import { WasiFilesystemTypes } from './interfaces/wasi-filesystem-types.js';
import { WasiIoError } from './interfaces/wasi-io-error.js';
import { WasiIoStreams } from './interfaces/wasi-io-streams.js';
import { WasiRandomRandom } from './interfaces/wasi-random-random.js';
export function stubWasi(engine: Uint8Array, features: Array<Features>, witWorld: string | undefined, witPath: string | undefined, worldName: string | undefined): Uint8Array;
export function spliceBindings(sourceName: string | undefined, spidermonkeyEngine: Uint8Array, witWorld: string | undefined, witPath: string | undefined, worldName: string | undefined, guestImports: Array<string>, guestExports: Array<string>, features: Array<Features>, debug: boolean): SpliceResult;
