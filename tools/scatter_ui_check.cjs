const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const source=fs.readFileSync(process.argv[2]||'client/game.js','utf8');
const display=source.slice(source.indexOf('async function displayRound('),source.indexOf('async function spin()'));
(async()=>{
for(const [name,initial,cascades,award,expected] of [
 ['initial',3,[3,4],10,['initial','bonus','tumble','tumble']],
 ['cascade',2,[2,3,4],10,['initial','tumble','tumble','bonus','tumble']],
 ['no bonus',2,[2],0,['initial','tumble']],
 ['old saved result',2,[3],0,['initial','tumble']]
]){
 const events=[],nodes={};const grid=n=>[Array.from({length:20},(_,i)=>i<n?'S':'K')];
 const context=vm.createContext({$:(id)=>nodes[id]??={classList:{toggle(){},remove(){}},textContent:''},document:{querySelectorAll:()=>[]},
 t:x=>x,update(){},playCue(){},rollReels:async()=>events.push('initial'),announce:async()=>events.push('bonus'),
 round:x=>x,fmt:x=>x,celebrateCascade:async()=>{},tumble:async()=>events.push('tumble'),snapshot(){},winTier:()=>null,celebrateWin(){}});
 vm.runInContext(display,context);
 await context.displayRound({betCents:500,balanceBeforeCents:10000,balanceCents:9500,freeSpins:award,freeSpin:false,outcome:{initialBoard:grid(initial),freeAward:award,cascades:cascades.map(n=>({hits:[],winCents:0,multiplier:1,nextBoard:grid(n)}))}});
 assert.deepEqual(events,expected);assert.equal(context.free,award);console.log('PASS UI '+name);
}
})().catch(e=>{console.error(e);process.exit(1)});
