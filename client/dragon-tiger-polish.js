'use strict';
// Presentation only: the existing server remains authoritative for cards, stakes and payouts.
Object.assign(filipino,{
 'Table sound':'Tunog ng mesa','Sound settings':'Mga setting ng tunog','Test sound':'Subukan ang tunog',
 'Table connected':'Konektado ang mesa','Reconnecting…':'Kumokonekta muli…','Place your bets':'Ilagay ang iyong taya',
 'No more bets':'Sarado na ang tayaan','Next game':'Susunod na laro','Round':'Laro',
 'Tap a result to inspect the cards.':'Pindutin ang resulta upang makita ang mga baraha.',
 'Last 20 games':'Huling 20 laro','Results are history, not predictions.':'Kasaysayan ang mga resulta, hindi hula.',
 'Sound starts after you tap a chip or Test sound.':'Magsisimula ang tunog matapos pindutin ang chip o Subukan ang tunog.',
 'Two-card variant':'Bersiyong dalawang baraha','Close':'Isara','Volume':'Lakas ng tunog',
 'Two cards per side · Last digit wins':'Dalawang baraha bawat panig · Huling digit ang puntos',
 'Winning side':'Panalong panig','Full screen':'Buong screen','Exit full screen':'Lumabas sa buong screen'
});
const dtToolbar=document.createElement('div');dtToolbar.className='dt-toolbar';
dtToolbar.innerHTML='<span class="dt-link-state" id="dtLinkState"><i></i><span></span></span><span id="dtRoundLabel"></span><button id="dtAudioButton" type="button" aria-haspopup="dialog">♫</button><button id="dtFullscreen" type="button" aria-label="Full screen">⛶</button>';
document.querySelector('.dt-heading').after(dtToolbar);
$('dtFullscreen').innerHTML='<svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="1.6" aria-hidden="true"><path d="M8 3H3v5m13-5h5v5M3 16v5h5m13-5v5h-5"/></svg>';
const dtStage=document.createElement('div');dtStage.className='dt-stage-message';dtStage.id='dtStageMessage';dtStage.setAttribute('role','status');$('dtTable').append(dtStage);
const dtAudioDialog=document.createElement('dialog');dtAudioDialog.id='dtAudioDialog';dtAudioDialog.className='dt-audio-dialog';dtAudioDialog.setAttribute('aria-labelledby','dtAudioTitle');
dtAudioDialog.innerHTML='<button type="button" class="icon close" id="dtAudioClose" aria-label="Close">×</button><p class="dt-eyebrow">DRAGON TIGER</p><h2 id="dtAudioTitle" data-i18n="Sound settings">Sound settings</h2><button type="button" id="dtAudioToggle"></button><label for="dtVolume" data-i18n="Volume">Volume</label><div class="dt-volume-row"><input id="dtVolume" type="range" min="0" max="100" step="1"><output id="dtVolumeValue"></output></div><button type="button" id="dtAudioTest" data-i18n="Test sound">Test sound</button><p data-i18n="Sound starts after you tap a chip or Test sound.">Sound starts after you tap a chip or Test sound.</p>';
document.body.append(dtAudioDialog);
const dtInspect=document.createElement('dialog');dtInspect.id='dtInspect';dtInspect.className='dt-inspect';dtInspect.setAttribute('aria-labelledby','dtInspectTitle');dtInspect.innerHTML='<button type="button" id="dtInspectClose" class="icon close" aria-label="Close">×</button><p class="dt-eyebrow" id="dtInspectRound"></p><h2 id="dtInspectTitle"></h2><div class="dt-inspect-hands"><section><h3>DRAGON <span id="dtInspectDragonScore"></span></h3><div class="dt-cards" id="dtInspectDragon"></div></section><section><h3>TIGER <span id="dtInspectTigerScore"></span></h3><div class="dt-cards" id="dtInspectTiger"></div></section></div>';
document.body.append(dtInspect);
const dtSummary=document.createElement('div');dtSummary.className='dt-history-summary';dtSummary.id='dtHistorySummary';$('dtRoad').before(dtSummary);
const dtHistoryHint=document.createElement('p');dtHistoryHint.className='dt-history-note';dtHistoryHint.dataset.i18n='Tap a result to inspect the cards.';dtHistoryHint.textContent='Tap a result to inspect the cards.';$('dtRoad').after(dtHistoryHint);
let dtPhaseKey='',dtNoiseBuffer=null;
const dtFlights=new Set();
try{const saved=JSON.parse(localStorage.getItem('ace-audio-preferences')||'null');if(saved&&typeof saved.sound==='boolean'&&Number.isFinite(saved.volume)){sound=saved.sound;volume=Math.max(0,Math.min(1,saved.volume));$('volume').value=String(Math.round(volume*100));soundUI()}}catch{}
function dtSaveAudio(){try{localStorage.setItem('ace-audio-preferences',JSON.stringify({sound,volume}))}catch{}dtAudioUI()}
function dtAudioUI(){
 $('dtAudioButton').textContent=sound&&volume?'♫':'♫ ×';$('dtAudioButton').setAttribute('aria-label',t('Table sound')+' · '+t(sound?'Sound on':'Sound off'));
 $('dtAudioToggle').textContent=t(sound?'Sound off':'Sound on');$('dtAudioToggle').setAttribute('aria-pressed',String(sound));$('dtVolume').value=String(Math.round(volume*100));$('dtVolumeValue').textContent=Math.round(volume*100)+'%';
 $('dtFullscreen').setAttribute('aria-label',t(document.fullscreenElement?'Exit full screen':'Full screen'));
}
function dtCue(kind){
 if(document.hidden||activeView!=='dragonTigerView'||!sound||!volume)return;
 if(kind==='chip'||kind==='deal'||kind==='flip'){
  const ctx=openAudio();if(!ctx||ctx.state!=='running')return;
  if(!dtNoiseBuffer||dtNoiseBuffer.sampleRate!==ctx.sampleRate){dtNoiseBuffer=ctx.createBuffer(1,Math.ceil(ctx.sampleRate*.15),ctx.sampleRate);const samples=dtNoiseBuffer.getChannelData(0);for(let i=0;i<samples.length;i++)samples[i]=Math.random()*2-1}
  const source=ctx.createBufferSource(),filter=ctx.createBiquadFilter(),gain=ctx.createGain();source.buffer=dtNoiseBuffer;filter.type='bandpass';filter.frequency.value=kind==='chip'?2600:kind==='deal'?900:1600;filter.Q.value=kind==='chip'?3:.8;
  const now=ctx.currentTime,duration=kind==='chip'?.045:.10;gain.gain.setValueAtTime(.001,now);gain.gain.linearRampToValueAtTime(kind==='chip'?.25:.16,now+.008);gain.gain.exponentialRampToValueAtTime(.001,now+duration);
  source.connect(filter);filter.connect(gain);gain.connect(masterGain);source.onended=()=>{source.disconnect();filter.disconnect();gain.disconnect()};source.start(now);source.stop(now+duration+.01);
 }else if(kind==='close'){tone(440,.10,'sine');tone(330,.16,'sine',.12)}
 else if(kind==='open'){tone(523,.09,'sine');tone(659,.13,'sine',.08)}
 else if(kind==='win'){[523,659,784,1047].forEach((n,i)=>tone(n,.18,'triangle',i*.09))}
 else if(kind==='tick')tone(880,.04,'sine');
 else if(kind==='select')tone(540,.045,'sine');
}
const dtOriginalSound=$('sound').onclick;$('sound').onclick=()=>{dtOriginalSound();dtSaveAudio()};
const dtOriginalVolume=$('volume').oninput;$('volume').oninput=e=>{dtOriginalVolume(e);dtSaveAudio()};
const dtOriginalTest=$('testSound').onclick;$('testSound').onclick=()=>{dtOriginalTest();dtSaveAudio()};
$('dtAudioButton').onclick=()=>{dtAudioUI();dtAudioDialog.showModal()};$('dtAudioClose').onclick=()=>dtAudioDialog.close();
$('dtAudioToggle').onclick=()=>{$('sound').click();dtAudioUI()};
$('dtVolume').oninput=e=>{$('volume').value=e.target.value;$('volume').dispatchEvent(new Event('input'));dtSaveAudio()};
$('dtAudioTest').onclick=async()=>{sound=true;if(volume===0){volume=.5;$('volume').value='50'}const ctx=openAudio();if(ctx?.state==='suspended')try{await ctx.resume()}catch{}soundUI();dtSaveAudio();dtCue('win')};
$('dtFullscreen').hidden=!document.fullscreenEnabled;$('dtFullscreen').onclick=async()=>{try{if(document.fullscreenElement)await document.exitFullscreen();else await $('dragonTigerView').requestFullscreen();dtAudioUI()}catch{toast(t('Something went wrong. Please retry.'))}};
$('dtInspectClose').onclick=()=>dtInspect.close();
function dtInspectGame(row){$('dtInspectRound').textContent=t('Round')+' #'+row.tableRoundId;$('dtInspectTitle').textContent=t(row.outcome.winner==='TIE'?'Tie game':row.outcome.winner==='DRAGON'?'Dragon wins':'Tiger wins');for(const side of ['Dragon','Tiger']){$('dtInspect'+side+'Score').textContent=row.outcome[side.toLowerCase()+'Score'];$('dtInspect'+side).replaceChildren(...row.outcome[side.toLowerCase()].map(c=>{const el=dtCard(c);el.classList.add('revealed');return el}))}dtInspect.showModal()}
const dtBaseHistoryRender=dtHistoryRender;dtHistoryRender=()=>{dtBaseHistoryRender();const rows=dtTableRows.slice(0,20);$('dtHistorySummary').replaceChildren(...['DRAGON','TIGER','TIE'].map(side=>{const el=document.createElement('span');el.className=side;el.textContent=(side==='TIE'?t('TIE'):side)+' '+rows.filter(r=>r.outcome.winner===side).length;return el}));[...$('dtRoad').children].forEach((el,i)=>{const row=rows.slice().reverse()[i];el.tabIndex=0;el.setAttribute('role','button');el.setAttribute('aria-haspopup','dialog');el.onclick=()=>dtInspectGame(row);el.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();dtInspectGame(row)}}});if($('dtRoad').lastElementChild)$('dtRoad').lastElementChild.classList.add('latest')};
const dtBaseUpdate=dtUpdate;dtUpdate=()=>{dtBaseUpdate();if(!$('dtLinkState'))return;const connected=Date.now()-dtLastSync<6500;$('dtLinkState').classList.toggle('offline',!connected);$('dtLinkState').querySelector('span').textContent=t(connected?'Table connected':'Reconnecting…');$('dtRoundLabel').textContent=dtTableState?t('Round')+' #'+dtTableState.roundId:'—';$('dtTable').classList.toggle('bets-closed',Boolean(dtTableState&&!dtBetting()));dtAudioUI()};
const dtBaseClock=dtClock;dtClock=()=>{dtBaseClock();if(!dtTableState||activeView!=='dragonTigerView')return;const key=dtTableState.roundId+':'+(dtBetting()?'open':'closed');if(key!==dtPhaseKey){const previous=dtPhaseKey;dtPhaseKey=key;$('dtStageMessage').textContent=t(dtBetting()?'Place your bets':'No more bets');$('dtStageMessage').classList.remove('show');if(!document.hidden){void $('dtStageMessage').offsetWidth;$('dtStageMessage').classList.add('show');if(previous)dtCue(dtBetting()?'open':'close')}}};
const dtBaseChipEffect=dtChipEffect;dtChipEffect=(side,value)=>{dtBaseChipEffect(side,value);if(document.hidden||matchMedia('(prefers-reduced-motion: reduce)').matches||dtFlights.size>=8)return;const source=document.querySelector('[data-dt-chip="'+value+'"]'),target=document.querySelector('[data-dt-side='+side+']');if(!source||!target)return;const a=source.getBoundingClientRect(),b=target.getBoundingClientRect();const coin=document.createElement('span');coin.className='dt-flying-chip';coin.textContent=value;coin.setAttribute('aria-hidden','true');coin.style.left=a.x+a.width/2-17+'px';coin.style.top=a.y+a.height/2-17+'px';document.body.append(coin);dtFlights.add(coin);const dx=b.x+b.width/2-a.x-a.width/2,dy=b.y+b.height*.65-a.y-a.height/2;const animation=coin.animate([{transform:'translate(0,0) scale(1)',opacity:1},{transform:'translate('+dx*.5+'px,'+(dy*.5-35)+'px) scale(1.12)',opacity:1,offset:.5},{transform:'translate('+dx+'px,'+dy+'px) scale(.75)',opacity:0}],{duration:420,easing:'cubic-bezier(.2,.65,.3,1)'});const done=()=>{coin.remove();dtFlights.delete(coin)};animation.onfinish=done;animation.oncancel=done};
document.addEventListener('visibilitychange',()=>{if(document.hidden){for(const coin of dtFlights)coin.remove();dtFlights.clear();$('dtStageMessage').classList.remove('show')}});
document.querySelectorAll('.dt-audio-dialog,.dt-inspect').forEach(dialog=>dialog.addEventListener('click',e=>{if(e.target===dialog){const r=dialog.getBoundingClientRect();if(e.clientX<r.left||e.clientX>r.right||e.clientY<r.top||e.clientY>r.bottom)dialog.close()}}));
dtAudioUI();dtHistoryRender();localize();
