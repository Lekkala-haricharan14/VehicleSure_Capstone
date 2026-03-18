import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LandingComponent } from './landing.component';
import { AuthService } from '../../../core/services/auth.service';
import { provideRouter } from '@angular/router';

describe('LandingComponent', () => {
    let component: LandingComponent;
    let fixture: ComponentFixture<LandingComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);

        await TestBed.configureTestingModule({
            imports: [LandingComponent],
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                provideRouter([])
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LandingComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should toggle FAQ', () => {
        expect(component.activeFaq()).toBeNull();
        component.toggleFaq(0);
        expect(component.activeFaq()).toBe(0);
        component.toggleFaq(0);
        expect(component.activeFaq()).toBeNull();
    });

    it('should set active tab', () => {
        component.setTab(2);
        expect(component.activeTab()).toBe(2);
    });

    it('should react to scroll window event', () => {
        expect(component.isScrolled()).toBeFalse();
        
        // Mocking window.scrollY
        Object.defineProperty(window, 'scrollY', { value: 100, writable: true });
        window.dispatchEvent(new Event('scroll'));
        
        expect(component.isScrolled()).toBeTrue();
    });
});
