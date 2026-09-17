'use strict';
Object.assign(filipino,{'Daily Lobby reward':'Araw-araw na gantimpala sa Lobby','Chips received':'Natanggap na chips','From':'Mula kay','Received':'Natanggap','Club chip transfers only. Contact your Agent for chips.':'Mga paglilipat ng chips sa Club lamang. Makipag-ugnayan sa iyong Agent para sa chips.'});
const receiptDialog=document.createElement('dialog');receiptDialog.id='chipReceipt';receiptDialog.setAttribute('aria-labelledby','receiptTitle');
receiptDialog.innerHTML='<div class="receipt-symbol" aria-hidden="true">◉</div><h2 id="receiptTitle" data-i18n="Chips received">Chips received</h2><strong id="receiptAmount"></strong><p id="receiptPlace">LUCKY SEVEN · 686868</p><p id="receiptSender"></p><button type="button" id="receiptRead" class="primary" data-i18n="Received">Received</button>';
document.body.append(receiptDialog);let receiptOwner=null,receiptCurrent=null,receiptPolling=false;
async function pollReceipts(){
 if(receiptPolling||!canPlay())return;
 if(receiptCurrent&&receiptOwner!==player.id){receiptDialog.close();receiptCurrent=null;}
 if(receiptCurrent||document.querySelector('dialog[open]'))return;
 const owner=player.id;receiptPolling=true;
 try{const notices=await api('notifications');if(!player||player.id!==owner||!canPlay()||document.querySelector('dialog[open]'))return;
  if(notices.length){receiptCurrent=notices[0];receiptOwner=owner;const gold=receiptCurrent.currency==='GOLD';$('receiptTitle').removeAttribute('data-i18n');$('receiptTitle').textContent=t(gold?'Daily Lobby reward':'Chips received');$('receiptPlace').textContent=gold?'LOBBY':'LUCKY SEVEN · 686868';$('receiptRead').textContent=t('Received');$('receiptAmount').textContent='+'+fmt(receiptCurrent.amountCents/100)+(gold?' Gold':' chips');$('receiptSender').textContent=(gold?'':t('From')+': ')+receiptCurrent.senderName;receiptDialog.showModal();}
 }catch{}finally{receiptPolling=false;}
}
async function acknowledgeReceipt(){if(!receiptCurrent)return;const button=$('receiptRead');if(button.disabled)return;button.disabled=true;try{await api('notifications/'+receiptCurrent.id+'/read','POST');receiptDialog.close();receiptCurrent=null;}catch(e){toast(errorText(e))}finally{button.disabled=false;}}
$('receiptRead').onclick=acknowledgeReceipt;receiptDialog.addEventListener('cancel',e=>{e.preventDefault();void acknowledgeReceipt()});
const beforeReceiptRole=applyRole;applyRole=()=>{beforeReceiptRole();if(!player||player.id!==receiptOwner){if(receiptDialog.open)receiptDialog.close();receiptCurrent=null;receiptOwner=null;}};
setInterval(()=>void pollReceipts(),3000);document.addEventListener('visibilitychange',()=>{if(!document.hidden)void pollReceipts()});
