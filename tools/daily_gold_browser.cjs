const {chromium}=require(process.env.ACE_PLAYWRIGHT||'playwright');
const assert=require('node:assert/strict');
(async()=>{const browser=await chromium.launch({executablePath:process.env.ACE_CHROMIUM,headless:true,args:['--no-sandbox'],env:{...process.env}});try{
 const page=await browser.newPage({viewport:{width:390,height:844}});
 await page.goto(process.env.ACE_TEST_URL);
 await page.waitForFunction(()=>typeof api==='function');
 await page.evaluate(async()=>{await api('login','POST',{username:'creator',password:'test-creator-password'});await connect();for(const d of document.querySelectorAll('dialog[open]'))d.close();});
 await page.waitForFunction(()=>document.querySelector('#chipReceipt')?.open,{},{timeout:20000});
 assert.equal(await page.locator('#receiptTitle').textContent(),'Daily Lobby reward');assert.match(await page.locator('#receiptAmount').textContent(),/10[,. ]?000 Gold/);assert.equal(await page.locator('#receiptPlace').textContent(),'LOBBY');
 const me=await page.evaluate(()=>api('me?mode=LOBBY'));assert.equal(me.lobbyGoldCents,1000000);assert.equal(me.clubChipsCents,0);
 await page.locator('#receiptRead').click();await page.waitForFunction(()=>!document.querySelector('#chipReceipt').open);assert.deepEqual(await page.evaluate(()=>api('notifications')),[]);
 await page.reload();await page.waitForFunction(()=>ready);assert.deepEqual(await page.evaluate(()=>api('notifications')),[]);
 console.log('PASS packaged JAR: scheduled offline grant, Gold popup on mobile, Club isolation, acknowledgment persists across reload.');
}finally{await browser.close();}})().catch(e=>{console.error(e);process.exit(1)});
