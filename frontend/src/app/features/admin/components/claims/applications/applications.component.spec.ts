import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminApplicationsComponent } from './applications.component';
import { AdminService } from '../../../services/admin.service';
import { of } from 'rxjs';
import { FormsModule } from '@angular/forms';

describe('AdminApplicationsComponent', () => {
    let component: AdminApplicationsComponent;
    let fixture: ComponentFixture<AdminApplicationsComponent>;
    let adminServiceSpy: jasmine.SpyObj<AdminService>;

    beforeEach(async () => {
        adminServiceSpy = jasmine.createSpyObj('AdminService', ['getAllApplications', 'getUnderwritersByWorkload', 'assignApplication']);
        adminServiceSpy.getAllApplications.and.returnValue(of([{ vehicleApplicationId: 1, status: 'UNDER_REVIEW' } as any]));
        adminServiceSpy.getUnderwritersByWorkload.and.returnValue(of([]));

        await TestBed.configureTestingModule({
            imports: [AdminApplicationsComponent, FormsModule],
            providers: [
                { provide: AdminService, useValue: adminServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AdminApplicationsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load applications and underwriters on init', () => {
        expect(adminServiceSpy.getAllApplications).toHaveBeenCalled();
        expect(component.applications().length).toBe(1);
    });

    it('should compute status counts correctly', () => {
        const counts = component.statusCounts();
        expect(counts.UNDER_REVIEW).toBe(1);
        expect(counts.ALL).toBe(1);
    });

    it('should assign application to underwriter', () => {
        component.selectedApplication.set({ vehicleApplicationId: 1 } as any);
        adminServiceSpy.assignApplication.and.returnValue(of({ vehicleApplicationId: 1, status: 'ASSIGNED' } as any));

        component.assignToUnderwriter('201');

        expect(adminServiceSpy.assignApplication).toHaveBeenCalledWith(1, 201);
    });
});
