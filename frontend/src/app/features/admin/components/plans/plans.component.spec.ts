import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminPlansComponent } from './plans.component';
import { AdminService } from '../../services/admin.service';
import { of, throwError } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

describe('AdminPlansComponent', () => {
    let component: AdminPlansComponent;
    let fixture: ComponentFixture<AdminPlansComponent>;
    let adminServiceSpy: jasmine.SpyObj<AdminService>;

    beforeEach(async () => {
        adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllPolicyPlans', 'addPolicyPlan', 'deactivatePolicyPlan', 'activatePolicyPlan']);
        adminServiceSpy.getAllPolicyPlans.and.returnValue(of([]));

        await TestBed.configureTestingModule({
            imports: [AdminPlansComponent, ReactiveFormsModule],
            providers: [
                { provide: AdminService, useValue: adminServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminPlansComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create and load plans', () => {
        expect(component).toBeTruthy();
        expect(adminServiceSpy.getAllPolicyPlans).toHaveBeenCalled();
    });

    it('should submit new plan', () => {
        component.planForm.patchValue({
            planName: 'New Plan',
            policyType: 'COMPREHENSIVE',
            description: 'Desc',
            basePremium: 1000,
            maxCoverageAmount: 100000,
            applicableVehicleType: 'CAR'
        });
        adminServiceSpy.addPolicyPlan.and.returnValue(of({} as any));

        component.submitPlan();

        expect(adminServiceSpy.addPolicyPlan).toHaveBeenCalled();
        expect(component.showPlanModal()).toBeFalse();
    });

    it('should deactivate plan', () => {
        component.deactivatePlan(1, 'Test Plan');
        expect(component.showDeactivateModal()).toBeTrue();
        
        adminServiceSpy.deactivatePolicyPlan.and.returnValue(of({ planId: 1, active: false } as any));
        component.confirmDeactivate();
        
        expect(adminServiceSpy.deactivatePolicyPlan).toHaveBeenCalledWith(1);
        expect(component.showDeactivateModal()).toBeFalse();
    });
});
