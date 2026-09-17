/* Bounded, view-scoped celebration. Only positive net returns qualify. */
'use strict';
const victorySeen=new Set();let victoryTimer=0,victoryFrame=0,victoryNode=null;
function hideVictory(){clearTimeout(victoryTimer);cancelAnimationFrame(victoryFrame);victoryNode?.remove();victoryNode=null;}
function celebrateWin(view,key,stake,returned){
 const net=returned-stake,owner=(player?.id||'guest')+':'+gameMode,unique=owner+':'+view+':'+key;
 if(!player||!Number.isFinite(net)||net<=0||document.hidden||activeView!==view||victorySeen.has(unique))return;
 victorySeen.add(unique);if(victorySeen.size>128)victorySeen.delete(victorySeen.values().next().value);hideVictory();
 const reduced=matchMedia('(prefers-reduced-motion: reduce)').matches,big=stake>0&&net>=stake*5;
 const box=document.createElement('div');victoryNode=box;box.className='ace-victory'+(big?' mega':'');box.setAttribute('role','status');box.innerHTML='<div class="av-rays" aria-hidden="true"></div><div class="av-sparks" aria-hidden="true"></div><div class="av-award"><span aria-hidden="true">✦</span><strong>'+t(big?'BIG WIN':'YOU WIN')+'</strong><small>'+t('Net win')+'</small><b></b><p>'+t('Total returned')+' '+money(returned)+' '+(gameMode==='LOBBY'?'Gold':'chips')+'</p></div>';
 const host=$(view);host.classList.add('victory-host');host.append(box);const amount=box.querySelector('b');amount.textContent='+'+money(net);
 if(!reduced){const sparks=box.querySelector('.av-sparks');for(let i=0;i<(big?28:18);i++){const coin=document.createElement('i');coin.textContent=i%3?'✦':'●';coin.style.cssText='--x:'+((i*37)%100)+'%;--dx:'+((i%7)-3)*32+'px;--delay:'+(i%7)*.055+'s;--spin:'+((i%2?1:-1)*270)+'deg';sparks.append(coin);}const start=performance.now();const tick=now=>{if(owner!==(player?.id||'guest')+':'+gameMode||activeView!==view){hideVictory();return}const p=Math.min(1,(now-start)/800);amount.textContent='+'+money(Math.round(net*(1-Math.pow(1-p,3))));if(p<1)victoryFrame=requestAnimationFrame(tick)};victoryFrame=requestAnimationFrame(tick);}
 if(sound&&volume){openAudio();[262,392,523,659,784,1047].forEach((hz,i)=>tone(hz,.15,'triangle',i*.07));}victoryTimer=setTimeout(hideVictory,2600);
}
document.addEventListener('visibilitychange',()=>{if(document.hidden)hideVictory()});
const victoryOldShow=showView;showView=async id=>{if(id!==activeView)hideVictory();return victoryOldShow(id)};
const victoryOldRole=applyRole;applyRole=()=>{hideVictory();victoryOldRole()};
