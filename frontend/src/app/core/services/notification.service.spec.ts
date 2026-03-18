import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
    let service: NotificationService;
    let httpMock: HttpTestingController;
    const API_URL = 'http://localhost:8080/api/notifications';

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [NotificationService]
        });
        service = TestBed.inject(NotificationService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should load notifications and update signal', () => {
        const mockNotifs = [{ id: 1, message: 'Test Notif', isRead: false }];
        
        service.loadNotifications();
        
        const req1 = httpMock.expectOne(`${API_URL}`);
        expect(req1.request.method).toBe('GET');
        req1.flush(mockNotifs);

        const req2 = httpMock.expectOne(`${API_URL}/unread-count`);
        expect(req2.request.method).toBe('GET');
        req2.flush(1);
        
        expect(service.notifications()).toEqual(mockNotifs as any);
        expect(service.unreadCount()).toBe(1);
    });

    it('should mark notification as read', () => {
        const initialNotifs = [{ id: 1, message: 'Test Notif', isRead: false }];
        service.notifications.set(initialNotifs as any);
        
        service.markAsRead(1);
        
        const req1 = httpMock.expectOne(`http://localhost:8080/api/notifications/1/read`);
        expect(req1.request.method).toBe('PUT');
        req1.flush({});

        const req2 = httpMock.expectOne(`http://localhost:8080/api/notifications/unread-count`);
        expect(req2.request.method).toBe('GET');
        req2.flush(0);
        
        expect(service.notifications()[0].isRead).toBe(true);
        expect(service.unreadCount()).toBe(0);
    });
});
