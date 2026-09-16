'use strict';
Object.assign(filipino,{'My players':'Mga manlalaro ko','Agents and players':'Mga agent at manlalaro','Club management':'Pamamahala ng club','My win / loss':'Aking panalo / talo','Player reports':'Mga ulat ng manlalaro','Creator can issue Club chips without an existing balance.':'Maaaring lumikha ang Creator ng chips sa Club kahit walang balanse.','Transfers use your Club chip balance and are limited to your own hierarchy.':'Gamit ang balanse ng chips mo sa Club, maaari lamang maglipat sa iyong mga nasasakupan.','Available Club chips':'Magagamit na chips sa Club'});
const transferHelp=document.createElement('p');transferHelp.className='muted';transferHelp.id='transferHelp';$('moveChipsForm').prepend(transferHelp);
const roleBase=applyRole;applyRole=()=>{
 roleBase();const role=player?.role;
 const accountTitle=role==='CREATOR'?'Club management':role==='SUPER_AGENT'?'Agents and players':'My players';
 const reportTitle=role==='PLAYER'?'My win / loss':'Player reports';
 for(const el of [document.querySelector('[data-view="accountsView"]'),$('accountsView').querySelector('h1')]){el.dataset.i18n=accountTitle;el.textContent=t(accountTitle)}
 for(const el of [document.querySelector('[data-view="reportsView"]'),$('reportsView').querySelector('h1')]){el.dataset.i18n=reportTitle;el.textContent=t(reportTitle)}
 transferHelp.textContent=role==='CREATOR'?t('Creator can issue Club chips without an existing balance.'):t('Available Club chips')+': '+fmt((player?.clubChipsCents||0)/100)+' · '+t('Transfers use your Club chip balance and are limited to your own hierarchy.');
};
const baseLanguage=setLanguage;setLanguage=value=>{baseLanguage(value);applyRole();modeUI()};
applyRole();

Object.assign(filipino,{
 'Join an Agent':'Sumali sa Agent','Agent ID':'ID ng Agent',
 'Send join request':'Ipadala ang kahilingan sa pagsali','Membership requests':'Mga kahilingan sa pagsali',
 'Already assigned to this Agent.':'Nasa Agent na ito ka na.',
 'A membership request is already pending.':'May nakabinbing kahilingan sa pagsali.',
 'Membership changed. Submit a new request.':'Nagbago ang namamahalang Agent. Magpadala ng bagong kahilingan.',
 'Remove player':'Alisin ang manlalaro','Removed':'Inalis','Club chips':'Chips sa Club',
 'Remove this player from your Agent group? They will remain in LUCKY SEVEN.':'Alisin ang manlalaro sa iyong grupo? Mananatili siya sa LUCKY SEVEN.',
 'Player removed from your group.':'Naalis ang manlalaro sa iyong grupo.',
 'The default club Agent cannot remove players from the default group.':'Hindi maaaring alisin ng default na Agent ang mga manlalaro mula sa default na grupo.'
});
Object.assign(filipino,{'Waiting for Agent approval. You have not joined this Agent yet.':'Naghihintay ng pag-apruba ng Agent. Hindi ka pa kasapi ng Agent na ito.','Request sent. The Agent must approve before you join.':'Naipadala ang kahilingan. Kailangang aprubahan ng Agent bago ka makasali.'});
const membershipPanel=document.createElement('section');
membershipPanel.hidden=true;
membershipPanel.innerHTML='<h2 data-i18n="Join an Agent">Join an Agent</h2><form id="agentJoinForm" class="form-grid"><label><span data-i18n="Agent ID">Agent ID</span><input name="agentCode" inputmode="numeric" pattern="[0-9]{6}" minlength="6" maxlength="6" required autocomplete="off"></label><button type="submit" data-i18n="Send join request">Send join request</button></form><div id="membershipStatus" role="status"></div>';
$('profileInfo').after(membershipPanel);
const inbox=document.createElement('section');inbox.hidden=true;
inbox.innerHTML='<h2 data-i18n="Membership requests">Membership requests</h2><button id="membershipRefresh" type="button" data-i18n="Refresh">Refresh</button><div id="membershipInbox" class="table-wrap"></div>';
$('accountsTable').before(inbox);
async function loadMembership(){
 const role=player?.role;
 if(!['PLAYER','AGENT'].includes(role))return;
 const owner=player.id,rows=await api('agent-membership');
 if(player?.id!==owner)return;
 if(role==='PLAYER'){
  $('membershipStatus').textContent=rows.map(r=>r.agentCode+' · '+t(({PENDING:'Waiting for Agent approval. You have not joined this Agent yet.',APPROVED:'Approved',REJECTED:'Rejected',REMOVED:'Removed'})[r.status]||r.status)).join('');
  $('agentJoinForm').querySelector('button').disabled=rows.some(r=>r.status==='PENDING');
 }else{
  table('membershipInbox',['ID','Username','Actions'],rows.map(r=>[esc(r.playerCode),esc(r.username),'<button data-join="'+esc(r.id)+'" data-approve="true">'+esc(t('Approve'))+'</button> <button data-join="'+esc(r.id)+'" data-approve="false">'+esc(t('Reject'))+'</button>']));
  inbox.querySelectorAll('[data-join]').forEach(button=>button.onclick=()=>task(async()=>{
   button.disabled=true;
   try{await api('agent-membership/decide','POST',{id:button.dataset.join,approve:button.dataset.approve==='true'});await loadAccounts();await loadMembership();}
   finally{button.disabled=false;}
  }));
 }
}
$('agentJoinForm').onsubmit=e=>{e.preventDefault();const form=e.currentTarget;if(!form.reportValidity())return;void task(async()=>{
 const button=form.querySelector('button');button.disabled=true;
 try{await api('agent-membership','POST',{agentCode:form.elements.agentCode.value});form.reset();toast(t('Request sent. The Agent must approve before you join.'));}
 finally{button.disabled=false;}
 await loadMembership();
})};
$('membershipRefresh').onclick=()=>task(loadMembership);
const membershipRoleBase=applyRole;applyRole=()=>{membershipRoleBase();membershipPanel.hidden=player?.role!=='PLAYER';inbox.hidden=player?.role!=='AGENT';};
const membershipViewBase=showView;showView=async id=>{await membershipViewBase(id);if(activeView===id&&((id==='profileView'&&player?.role==='PLAYER')||(id==='accountsView'&&player?.role==='AGENT')))await task(loadMembership);};
const membershipErrorBase=errorText;errorText=e=>{
 const messages={ALREADY_MEMBER:'Already assigned to this Agent.',JOIN_PENDING:'A membership request is already pending.',MEMBERSHIP_CHANGED:'Membership changed. Submit a new request.'};
 return messages[e.code]?t(messages[e.code]):membershipErrorBase(e);
};
applyRole();localize();

const removeDialog=document.createElement('dialog');
removeDialog.id='removePlayerDialog';
removeDialog.setAttribute('aria-labelledby','removePlayerTitle');
removeDialog.innerHTML='<h2 id="removePlayerTitle" data-i18n="Remove player">Remove player</h2><p id="removePlayerIdentity"></p><p data-i18n="Remove this player from your Agent group? They will remain in LUCKY SEVEN.">Remove this player from your Agent group? They will remain in LUCKY SEVEN.</p><form id="removePlayerForm"><p id="removePlayerError" class="error" role="alert"></p><div class="inline"><button type="button" id="removePlayerCancel" data-i18n="Cancel">Cancel</button><button type="submit" data-i18n="Remove player">Remove player</button></div></form>';
document.body.append(removeDialog);
let removal=null;
const removalRequests=new Map();
$('removePlayerCancel').onclick=()=>removeDialog.close();
const removableAccountsBase=loadAccounts;
loadAccounts=async()=>{
 const owner=player?.id;
 await removableAccountsBase();
 if(player?.id!==owner||player?.role!=='AGENT')return;
 const children=accountRows.filter(a=>a.role==='PLAYER'&&a.parentId===owner);
 table('accountsTable',['ID','Username','Display name','Club chips','Actions'],children.map(a=>[esc(a.publicCode),esc(a.username),esc(a.displayName),'<strong class="number">'+esc(money(a.clubChipsCents))+'</strong>','<button type="button" data-remove-player="'+esc(a.id)+'">'+esc(t('Remove player'))+'</button>']));
 $('accountsTable').querySelectorAll('[data-remove-player]').forEach(button=>button.onclick=()=>{
  const target=children.find(a=>a.id===button.dataset.removePlayer);
  if(player?.id!==owner||player?.role!=='AGENT'||!target)return;
  const key=owner+':'+target.id;
  if(!removalRequests.has(key))removalRequests.set(key,requestUuid());
  removal={owner,key,playerId:target.id,requestId:removalRequests.get(key)};
  $('removePlayerIdentity').textContent=target.displayName+' · '+target.username+' · '+target.publicCode;
  $('removePlayerError').textContent='';
  removeDialog.showModal();
 });
};
$('removePlayerForm').onsubmit=async event=>{
 event.preventDefault();const selected=removal,button=event.currentTarget.querySelector('[type="submit"]');
 if(button.disabled||!selected||player?.id!==selected.owner||player?.role!=='AGENT')return;
 button.disabled=true;$('removePlayerCancel').disabled=true;$('removePlayerError').textContent='';
 try{
  await api('agent-membership/remove','POST',{playerId:selected.playerId,requestId:selected.requestId});
  removalRequests.delete(selected.key);removeDialog.close();removal=null;
  if(player?.id===selected.owner){toast(t('Player removed from your group.'));await task(async()=>{await loadAccounts();await loadMembership();});}
 }catch(error){$('removePlayerError').textContent=errorText(error);}
 finally{button.disabled=false;$('removePlayerCancel').disabled=false;}
};
localize();
