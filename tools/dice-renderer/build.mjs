import {build} from 'esbuild';
import {fileURLToPath} from 'node:url';
const root=fileURLToPath(new URL('../../',import.meta.url));
for(const [entry,out] of [['scene.js','color-dice-3d.js'],['physics.js','dice-physics-worker.js']])await build({entryPoints:[fileURLToPath(new URL(entry,import.meta.url))],bundle:true,minify:true,format:'iife',outfile:root+'client/'+out});
