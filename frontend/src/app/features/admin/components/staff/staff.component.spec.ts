import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminStaffComponent } from './staff.component';
import { AdminService } from '../../services/admin.service';
import { of } from 'rxjs';
import { ReactiveFormsModule } from '@angular/forms';

describe('AdminStaffComponent', () => {
    let component: AdminStaffComponent;
    let fixture: ComponentFixture<AdminStaffComponent>;
    let adminServiceSpy: jasmine.SpyObj<AdminService>;

    beforeEach(async () => {
        adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllStaff', 'createStaff', 'deactivateStaff', 'activateStaff']);
        adminServiceSpy.getAllStaff.and.returnValue(of([]));

        await TestBed.configureTestingModule({
            imports: [AdminStaffComponent, ReactiveFormsModule],
            providers: [
                { provide: AdminService, useValue: adminServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminStaffComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create and load staff', () => {
        expect(component).toBeTruthy();
        expect(adminServiceSpy.getAllStaff).toHaveBeenCalled();
    });

    it('should submit new staff', () => {
        component.staffForm.patchValue({
            username: 'newstaff',
            email: 'staff@test.com',
            password: 'password123',
            phoneNumber: '9876543210',
            role: 'UNDERWRITER'
        });
        adminServiceSpy.createStaff.and.returnValue(of({} as any));

        component.submitStaff();

        expect(adminServiceSpy.createStaff).toHaveBeenCalled();
        expect(component.showStaffModal()).toBeFalse();
    });
});
