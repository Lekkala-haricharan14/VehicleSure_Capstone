import { test, expect } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';

test('debug buy policy', async ({ page }) => {
    // 1. Create dummy files
    const dummyRc = path.join(__dirname, 'dummy_rc.pdf');
    const dummyInv = path.join(__dirname, 'dummy_inv.pdf');
    fs.writeFileSync(dummyRc, 'dummy content');
    fs.writeFileSync(dummyInv, 'dummy content');

    // Monitor all responses to check for 403
    page.on('response', response => {
        if (response.url().includes('/buy-policy')) {
            console.log('\n--- BUY POLICY RESPONSE ---');
            console.log('Status:', response.status());
            console.log('Headers:', response.headers());
            response.text().then(t => console.log('Body:', t)).catch(() => { });
        }
    });

    // 2. Login
    await page.goto('http://localhost:4200/login');
    await page.fill('input[placeholder="Enter your email address"]', 'customer@example.com');
    await page.fill('input[placeholder="Enter your password"]', 'password123');
    await page.click('button:has-text("Sign in securely")');
    await page.waitForURL('http://localhost:4200/customer');

    console.log('Logged in successfully');

    // 3. Go to Buy Policy
    await page.goto('http://localhost:4200/customer/purchase/car');

    // 4. Fill form
    await page.fill('input[formControlName="vehicleOwnerName"]', 'Test User');
    await page.fill('input[formControlName="registrationNumber"]', 'KA01AB1234');
    await page.fill('input[formControlName="make"]', 'Toyota');
    await page.fill('input[formControlName="model"]', 'Fortuner');
    await page.fill('input[formControlName="year"]', '2023');
    await page.selectOption('select[formControlName="fuelType"]', 'PETROL');
    await page.fill('input[formControlName="chassisNumber"]', 'CHASSIS987654321');
    await page.fill('input[formControlName="distanceDriven"]', '5000');
    await page.fill('input[formControlName="exShowroomPrice"]', '3500000');

    // Select Plan
    await page.click('div.grid > div:nth-child(1) input[type="radio"]'); // Pick first plan

    // Upload files
    await page.setInputFiles('input[type="file"]:nth-child(1)', dummyRc);

    // Note: Due to standard input[type=file] selecting, we might need a more precise loc.
    const fileInputs = await pageLocator('input[type="file"]');
    await fileInputs.nth(0).setInputFiles(dummyRc);
    await fileInputs.nth(1).setInputFiles(dummyInv);

    // Click Get Quote (Go to Step 2)
    await page.click('button:has-text("Calculate IDV & Get Quote")');

    // Select tenure
    await page.click('div.grid > div:nth-child(2) input[type="radio"]'); // 2 years

    // Proceed to checkout
    await page.click('button:has-text("Proceed to Checkout / Submit Application")');

    // Wait for the redirect
    await page.waitForTimeout(3000);
    console.log('End of script.');
});

// Helper for playwright locator workaround in script
function pageLocator(selector: string) { return selector; }
