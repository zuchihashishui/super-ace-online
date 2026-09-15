'use strict';
// Reuse the real navigation: no duplicate IDs, permissions or click handlers.
(()=>{
  const nav=document.querySelector('.app-nav'),header=document.querySelector('body > header');
  const anchor=document.createComment('desktop navigation');nav.before(anchor);
  const trigger=document.createElement('button');trigger.id='menuToggle';trigger.className='icon menu-toggle';trigger.type='button';
  trigger.innerHTML='<span aria-hidden="true">☰</span>';trigger.setAttribute('aria-controls','mobileMenu');trigger.setAttribute('aria-expanded','false');trigger.setAttribute('aria-haspopup','dialog');header.prepend(trigger);
  const drawer=document.createElement('dialog');drawer.id='mobileMenu';drawer.setAttribute('aria-labelledby','menuTitle');
  drawer.innerHTML='<div class="drawer-head"><div><p class="eyebrow">SUPER ACE</p><h2 id="menuTitle">Menu</h2></div><button type="button" class="icon" id="menuClose">×</button></div><p id="menuIdentity" class="menu-identity"></p><div class="drawer-foot">Virtual chips only</div>';
  document.body.append(drawer);
  const media=matchMedia('(max-width: 760px)');let returnFocus=false;
  function labels(){const fil=language==='fil';trigger.setAttribute('aria-label',fil?'Buksan ang menu':'Open menu');$('menuClose').setAttribute('aria-label',fil?'Isara ang menu':'Close menu');$('menuIdentity').textContent=player?player.displayName+' · '+player.role.replaceAll('_',' ')+' · ID '+player.publicCode:(fil?'Bisita · Mag-sign in para maglaro':'Guest · Sign in to play');drawer.querySelector('.drawer-foot').textContent=t('Virtual chips');nav.querySelectorAll('[data-view]').forEach(b=>{if(b.dataset.view===activeView)b.setAttribute('aria-current','page');else b.removeAttribute('aria-current')})}
  function closed(){trigger.setAttribute('aria-expanded','false');document.body.classList.remove('menu-open');if(returnFocus&&media.matches)trigger.focus();returnFocus=false}
  function close(focus=true){returnFocus=focus;drawer.close();closed()}
  trigger.addEventListener('click',()=>{labels();drawer.showModal();trigger.setAttribute('aria-expanded','true');document.body.classList.add('menu-open');$('menuClose').focus()});
  $('menuClose').onclick=()=>close();drawer.addEventListener('cancel',()=>{returnFocus=true});drawer.addEventListener('close',closed);
  drawer.addEventListener('click',e=>{if(e.target===drawer){const r=drawer.getBoundingClientRect();if(e.clientX<r.left||e.clientX>r.right||e.clientY<r.top||e.clientY>r.bottom)close()}});
  nav.addEventListener('click',e=>{if(e.target.closest('button')&&drawer.open)close(false)},true);
  function layout(){if(drawer.open)close(false);if(media.matches)drawer.querySelector('.drawer-foot').before(nav);else anchor.after(nav);trigger.hidden=!media.matches;labels()}
  media.addEventListener('change',layout);layout();
  const bonus=document.createElement('div');bonus.className='mobile-bonus';bonus.setAttribute('role','status');document.querySelector('.layout').after(bonus);
  function freeCount(){bonus.textContent=t('FREE SPINS')+' · '+$('free').textContent;bonus.hidden=Number($('free').textContent)===0}
  new MutationObserver(freeCount).observe($('free'),{childList:true,characterData:true,subtree:true});freeCount();
  new MutationObserver(labels).observe($('connection'),{childList:true,characterData:true,subtree:true});
  nav.addEventListener('click',()=>{labels();const page=document.getElementById(activeView);if(page&&!drawer.open&&!document.querySelector('dialog[open]')){const heading=page.querySelector('h1');if(heading){heading.tabIndex=-1;heading.focus({preventScroll:true})}}});
  $('language').addEventListener('change',labels);$('loginLanguage').addEventListener('click',labels);
  document.querySelectorAll('.table-wrap').forEach(el=>{el.tabIndex=0;el.setAttribute('role','region');el.setAttribute('aria-label',el.closest('.page')?.querySelector('h1')?.textContent||'Records')});
})();
