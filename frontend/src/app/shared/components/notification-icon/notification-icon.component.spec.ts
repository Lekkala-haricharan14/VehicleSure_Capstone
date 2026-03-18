import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationIconComponent } from './notification-icon.component';
import { NotificationService } from '../../../core/services/notification.service';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { signal } from '@angular/core';

describe('NotificationIconComponent', () => {
    let component: NotificationIconComponent;
    let fixture: ComponentFixture<NotificationIconComponent>;
    let notificationServiceSpy: jasmine.SpyObj<NotificationService>;
    let routerSpy: jasmine.SpyObj<Router>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        notificationServiceSpy = jasmine.createSpyObj('NotificationService', ['loadNotifications', 'markAsRead']);
        // Mock signals
        (notificationServiceSpy as any).notifications = signal([]);
        (notificationServiceSpy as any).unreadCount = signal(2);

        routerSpy = jasmine.createSpyObj('Router', ['navigate']);
        authServiceSpy = jasmine.createSpyObj('AuthService', ['logout'], {
            userRole: signal('ADMIN'),
            username: signal('admin')
        });

        await TestBed.configureTestingModule({
            imports: [NotificationIconComponent],
            providers: [
                { provide: NotificationService, useValue: notificationServiceSpy },
                { provide: Router, useValue: routerSpy },
                { provide: AuthService, useValue: authServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(NotificationIconComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create and load notifications', () => {
        expect(component).toBeTruthy();
        expect(notificationServiceSpy.loadNotifications).toHaveBeenCalled();
    });

    it('should toggle dropdown', () => {
        expect(component.isOpen).toBeFalse();
        component.toggleDropdown();
        expect(component.isOpen).toBeTrue();
    });

    it('should navigate and mark as read when clicking a notification', () => {
        const mockNotif = { id: 1, message: 'New Application', type: 'NEW_APPLICATION', isRead: false };
        
        component.onNotificationClick(mockNotif);
        
        expect(notificationServiceSpy.markAsRead).toHaveBeenCalledWith(1);
        expect(routerSpy.navigate).toHaveBeenCalledWith(['/admin/applications']);
    });
});
