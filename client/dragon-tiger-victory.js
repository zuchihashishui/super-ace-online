'use strict';
Object.assign(filipino,{'YOU WIN':'PANALO KA','BIG WIN':'MALAKING PANALO','Net win':'Netong panalo','Total returned':'Kabuuang ibinalik'});
const dtVictory=document.createElement('div');dtVictory.id='dtVictory';dtVictory.className='dt-victory';dtVictory.hidden=true;dtVictory.setAttribute('role','status');dtVictory.setAttribute('aria-live','polite');dtVictory.innerHTML='<div class="dt-victory-rays" aria-hidden="true"></div><div class="dt-victory-coins" aria-hidden="true"></div><div class="dt-victory-medallion" aria-hidden="true">♛</div><small id="dtVictorySide"></small><strong id="dtVictoryTitle"></strong><span class="dt-victory-label" id="dtVictoryLabel"></span><b id="dtVictoryAmount"></b><p id="dtVictoryDetail"></p>';$('dtTable').append(dtVictory);
let dtVictoryTimer=null,dtVictoryFrame=null,dtVictoryKey='',dtVictoryRow=null;
function dtHideVictory(){clearTimeout(dtVictoryTimer);cancelAnimationFrame(dtVictoryFrame);dtVictoryTimer=null;dtVictoryFrame=null;dtVictory.hidden=true;dtVictoryRow=null;dtVictory.querySelector('.dt-victory-coins').replaceChildren();$('dtTable').classList.remove('personal-win','personal-big-win');}
function dtVictoryText(row){const net=row.payoutCents-row.betCents,big=net>=row.betCents*5;const winner=row.outcome.winner;$('dtVictoryTitle').textContent=t(big?'BIG WIN':'YOU WIN');$('dtVictoryLabel').textContent=t('Net win');$('dtVictorySide').textContent=t(winner==='DRAGON'?'Dragon wins':winner==='TIGER'?'Tiger wins':'Tie game');$('dtVictoryDetail').textContent=t('Total returned')+' '+money(row.payoutCents)+' '+(gameMode==='LOBBY'?'Gold':'chips');$('dtVictoryAmount').textContent='+'+money(net);}
function dtCelebrate(row){
 const net=row.payoutCents-row.betCents,key=dtKey()+':'+row.tableRoundId;
 if(!player||row.betCents<=0||net<=0||key===dtVictoryKey||document.hidden||activeView!=='dragonTigerView')return;
 dtHideVictory();dtVictoryKey=key;dtVictoryRow=row;dtVictoryText(row);dtVictory.hidden=false;$('dtTable').classList.add('personal-win');if(net>=row.betCents*5)$('dtTable').classList.add('personal-big-win');
 const reduced=matchMedia('(prefers-reduced-motion: reduce)').matches;
 if(!reduced){
  const coins=dtVictory.querySelector('.dt-victory-coins');for(let i=0;i<18;i++){const coin=document.createElement('i');coin.textContent='✦';coin.style.setProperty('--x',((i*37)%100)+'%');coin.style.setProperty('--delay',(i%6)*.09+'s');coin.style.setProperty('--drift',((i%5)-2)*35+'px');coins.append(coin);}
  const started=performance.now();const tick=now=>{const progress=Math.min(1,(now-started)/650),amount=Math.round(net*(1-Math.pow(1-progress,3)));$('dtVictoryAmount').textContent='+'+money(amount);if(progress<1)dtVictoryFrame=requestAnimationFrame(tick);};dtVictoryFrame=requestAnimationFrame(tick);
 }
 if(sound&&volume){tone(131,.3,'sine');tone(262,.26,'triangle',.12);tone(1047,.22,'sine',.35);}
 dtVictoryTimer=setTimeout(dtHideVictory,3000);
}
const dtBeforeVictoryAnimate=dtAnimate;dtAnimate=async row=>{await dtBeforeVictoryAnimate(row);dtCelebrate(row);};
const dtBeforeVictoryClear=dtClear;dtClear=()=>{dtHideVictory();dtBeforeVictoryClear();};
const dtBeforeVictoryLanguage=setLanguage;setLanguage=value=>{dtBeforeVictoryLanguage(value);if(dtVictoryRow)dtVictoryText(dtVictoryRow);};
const dtBeforeVictoryRole=applyRole;applyRole=()=>{dtBeforeVictoryRole();dtHideVictory();};
document.addEventListener('visibilitychange',()=>{if(document.hidden)dtHideVictory();});
