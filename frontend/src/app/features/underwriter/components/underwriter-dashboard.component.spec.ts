import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnderwriterDashboardComponent } from './underwriter-dashboard.component';
import { UnderwriterService } from '../services/underwriter.service';
import { of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { signal } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('UnderwriterDashboardComponent', () => {
    let component: UnderwriterDashboardComponent;
    let fixture: ComponentFixture<UnderwriterDashboardComponent>;
    let underwriterServiceSpy: jasmine.SpyObj<UnderwriterService>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        underwriterServiceSpy = jasmine.createSpyObj('UnderwriterService', ['getAssignedApplications', 'approveApplication', 'rejectApplication']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['logout'], {
            userRole: signal('UNDERWRITER'),
            username: signal('underwriter')
        });

        underwriterServiceSpy.getAssignedApplications.and.returnValue(of([
            { vehicleApplicationId: 1, status: 'ASSIGNED', registrationNumber: 'REG1' } as any
        ]));
        underwriterServiceSpy.approveApplication.and.returnValue(of(undefined));
        underwriterServiceSpy.rejectApplication.and.returnValue(of(undefined));

        await TestBed.configureTestingModule({
            imports: [UnderwriterDashboardComponent, HttpClientTestingModule],
            providers: [
                { provide: UnderwriterService, useValue: underwriterServiceSpy },
                { provide: AuthService, useValue: authServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(UnderwriterDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load assigned applications on init', () => {
        expect(underwriterServiceSpy.getAssignedApplications).toHaveBeenCalled();
        expect(component.applications().length).toBe(1);
    });

    it('should open review modal', () => {
        const app = { vehicleApplicationId: 1, status: 'ASSIGNED' } as any;
        component.openReviewModal(app);
        expect(component.selectedApplication()).toBe(app);
    });
});
