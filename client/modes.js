'use strict';
Object.assign(filipino,{
 'Repeat password':'Ulitin ang password',
 'Gold for Lobby play. Club chips are separate.':'Gold para sa Lobby. Hiwalay ang chips ng Club.',
 'Club chips are provided by your Agent or club management.':'Ang chips ng Club ay mula sa iyong Agent o tagapamahala ng club.',
 'Finish the current spin, free spins and autoplay before switching.':'Tapusin ang kasalukuyang ikot, libreng ikot at autoplay bago lumipat.',
 'Club results only. Lobby Gold is excluded.':'Resulta ng Club lamang. Hindi kasama ang Gold ng Lobby.'
});
function modeUI(){
 if(!$('chooseLobby'))return;
 const locked=modeChanging||busy||polling||Boolean(pending)||autoActive()||free>0;
 const lobby=gameMode==='LOBBY';
 $('chooseLobby').disabled=locked||Boolean(player&&player.role!=='PLAYER');$('chooseClub').disabled=locked;
 $('chooseLobby').setAttribute('aria-pressed',String(lobby));$('chooseClub').setAttribute('aria-pressed',String(!lobby));
 $('lobbyFunds').textContent=(player?fmt((lobby?balance*100:player.lobbyGoldCents)/100):'—')+' Gold';
 $('clubFunds').textContent=(player?fmt((!lobby?balance*100:player.clubChipsCents)/100):'—')+' chips';
 $('currencyLabel').removeAttribute('data-i18n');$('currencyLabel').textContent=lobby?'GOLD · LOBBY':'CHIPS · CLUB';
 $('modeHint').textContent=t(locked?'Finish the current spin, free spins and autoplay before switching.':lobby?'Gold for Lobby play. Club chips are separate.':'Club chips are provided by your Agent or club management.');
 document.body.dataset.playMode=gameMode;
}
async function switchMode(next){
 if(!['LOBBY','CLUB'].includes(next)||next===gameMode||modeChanging||busy||polling||pending||autoActive()||free>0)return;
 if(player&&player.role!=='PLAYER')return;
 const previous=gameMode;modeChanging=true;gameMode=next;update();
 try{
  if(player){const wallet=await api('me');applyWallet(wallet);ready=wallet.role==='PLAYER';}
  last=0;seenRound=0;autoRun=null;pending=null;
  try{const saved=JSON.parse(sessionStorage.getItem('ace-pending-'+gameMode)||'null');if(saved?.playerId===player?.id&&saved?.mode===gameMode)pending=saved;sessionStorage.setItem('ace-mode',gameMode)}catch{}
  $('netResult').textContent='';$('chainValue').textContent='×1';
  document.querySelectorAll('.multi').forEach((el,i)=>el.classList.toggle('active',i===0));
  $('status').textContent=gameMode==='LOBBY'?'LOBBY · Gold':'LUCKY SEVEN · 686868 · chips';
 }catch(error){gameMode=previous;toast(errorText(error));}
 finally{modeChanging=false;update();}
 await loadRtp();await syncAuto(false);
}
$('chooseLobby').onclick=()=>switchMode('LOBBY');$('chooseClub').onclick=()=>switchMode('CLUB');
// Migrate a pending request from the one-wallet release into CLUB only.
try{const old=sessionStorage.getItem('ace-pending');if(old){const saved=JSON.parse(old);if(saved){saved.mode='CLUB';sessionStorage.setItem('ace-pending-CLUB',JSON.stringify(saved));}sessionStorage.removeItem('ace-pending')}}catch{}
const beforeRole=applyRole;applyRole=()=>{beforeRole();if(player)$('profileInfo').textContent+=' · LUCKY SEVEN (686868) · '+(player.role==='PLAYER'?'LOBBY '+fmt(player.lobbyGoldCents/100)+' Gold · ':'')+'CLUB '+fmt(player.clubChipsCents/100)+' chips';};
modeUI();localize();
