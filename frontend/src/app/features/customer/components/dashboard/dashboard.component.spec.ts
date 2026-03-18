import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { CustomerService } from '../../services/customer.service';
import { of } from 'rxjs';
import { signal } from '@angular/core';
import { AuthService } from '../../../../core/services/auth.service';
import { RouterTestingModule } from '@angular/router/testing';

describe('DashboardComponent', () => {
    let component: DashboardComponent;
    let fixture: ComponentFixture<DashboardComponent>;
    let customerServiceSpy: jasmine.SpyObj<CustomerService>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        customerServiceSpy = jasmine.createSpyObj('CustomerService', ['getMyApplications']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['logout'], {
            userRole: signal('CUSTOMER'),
            username: signal('customer')
        });

        customerServiceSpy.getMyApplications.and.returnValue(of([{ id: 1, status: 'PENDING' } as any]));

        await TestBed.configureTestingModule({
            imports: [DashboardComponent, RouterTestingModule],
            providers: [
                { provide: CustomerService, useValue: customerServiceSpy },
                { provide: AuthService, useValue: authServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(DashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should load data on init', () => {
        expect(customerServiceSpy.getMyApplications).toHaveBeenCalled();
        expect(component.applications.length).toBe(1);
        expect(component.isLoading).toBeFalse();
    });

    it('should compute active applications correctly', () => {
        expect(component.applications.length).toBe(1);
    });
});
