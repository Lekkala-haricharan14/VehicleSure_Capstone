import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminClaimsComponent } from './admin-claims.component';
import { AdminService } from '../../services/admin.service';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('AdminClaimsComponent', () => {
    let component: AdminClaimsComponent;
    let fixture: ComponentFixture<AdminClaimsComponent>;
    let adminServiceSpy: jasmine.SpyObj<AdminService>;

    beforeEach(async () => {
        adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllClaims', 'getClaimsOfficerWorkload', 'assignClaim']);
        adminServiceSpy.getAllClaims.and.returnValue(of([{ claimId: 1, status: 'SUBMITTED' } as any]));
        adminServiceSpy.getClaimsOfficerWorkload.and.returnValue(of([]));

        await TestBed.configureTestingModule({
            imports: [AdminClaimsComponent, FormsModule],
            providers: [
                { provide: AdminService, useValue: adminServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminClaimsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load claims and workloads on init', () => {
        expect(adminServiceSpy.getAllClaims).toHaveBeenCalled();
        expect(adminServiceSpy.getClaimsOfficerWorkload).toHaveBeenCalled();
        expect(component.claims().length).toBe(1);
    });

    it('should filter claims by status', () => {
        component.statusFilter.set('APPROVED');
        expect(component.filteredClaims().length).toBe(0);
        component.statusFilter.set('SUBMITTED');
        expect(component.filteredClaims().length).toBe(1);
    });

    it('should open assign modal', () => {
        const claim = { claimId: 1 } as any;
        component.openAssignModal(claim);
        expect(component.selectedClaim()).toBe(claim);
        expect(component.showModal).toBeTrue();
    });

    it('should assign claim to officer', () => {
        component.selectedClaim.set({ claimId: 1 } as any);
        component.selectedOfficerId = 101;
        adminServiceSpy.assignClaim.and.returnValue(of({} as any));

        component.assignToOfficer();

        expect(adminServiceSpy.assignClaim).toHaveBeenCalledWith(1, 101);
    });
});
