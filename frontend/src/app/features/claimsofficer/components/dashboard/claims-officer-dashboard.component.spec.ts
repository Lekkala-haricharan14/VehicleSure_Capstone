import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimsOfficerDashboardComponent } from './claims-officer-dashboard.component';
import { ClaimsOfficerService } from '../../services/claims-officer.service';
import { of } from 'rxjs';
import { signal } from '@angular/core';

describe('ClaimsOfficerDashboardComponent', () => {
    let component: ClaimsOfficerDashboardComponent;
    let fixture: ComponentFixture<ClaimsOfficerDashboardComponent>;
    let claimsServiceSpy: jasmine.SpyObj<ClaimsOfficerService>;

    beforeEach(async () => {
        claimsServiceSpy = jasmine.createSpyObj('ClaimsOfficerService', ['getAssignedClaims', 'approveClaim', 'rejectClaim', 'calculatePayment']);
        claimsServiceSpy.getAssignedClaims.and.returnValue(of([
            { claimId: 1, claimNumber: 'CLM001', status: 'ASSIGNED' } as any
        ]));
        claimsServiceSpy.approveClaim.and.returnValue(of(undefined));
        claimsServiceSpy.rejectClaim.and.returnValue(of(undefined));
        claimsServiceSpy.calculatePayment.and.returnValue(of(1000));

        await TestBed.configureTestingModule({
            imports: [ClaimsOfficerDashboardComponent],
            providers: [
                { provide: ClaimsOfficerService, useValue: claimsServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ClaimsOfficerDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load assigned claims on init', () => {
        expect(claimsServiceSpy.getAssignedClaims).toHaveBeenCalled();
        expect(component.claims().length).toBe(1);
    });

    it('should open review modal', () => {
        const claim = { claimId: 1, status: 'ASSIGNED' } as any;
        component.openReviewModal(claim);
        expect(component.selectedClaim()).toBe(claim);
        expect(component.showActionModal).toBeTrue();
    });
});
