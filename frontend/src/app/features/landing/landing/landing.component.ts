import { Component, signal, inject, OnInit, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.css',
})
export class LandingComponent implements OnInit {
    auth = inject(AuthService);
    isScrolled = signal(false);
    activeTab = signal(0);
    activeFaq = signal<number | null>(null);



    vehicleTypes = signal([
        {
            id: 'car',
            name: 'Car Insurance',
            desc: 'Comprehensive & Third-party cover for all Hatchbacks, Sedans, and SUVs.',
            tag: 'Most Popular',
            tagColor: '#4f46e5',
            color: '#312e81',
            bg: 'rgba(79, 70, 229, 0.08)',
        },
        {
            id: 'bike',
            name: 'Bike Insurance',
            desc: 'Protection for your Two-wheelers with instant policy issuance and renewal.',
            tag: 'Affordable',
            tagColor: '#6366f1',
            color: '#3730a3',
            bg: 'rgba(99, 102, 241, 0.08)',
        },
        {
            id: 'auto',
            name: 'Auto Rickshaw',
            desc: 'Reliable commercial cover for three-wheelers and passenger vehicles.',
            tag: 'Commercial',
            tagColor: '#818cf8',
            color: '#4338ca',
            bg: 'rgba(129, 140, 248, 0.08)',
        },
        {
            id: 'heavy',
            name: 'Heavy Vehicle',
            desc: 'Enterprise-grade fleet insurance for trucks, buses, and logistics vehicles.',
            tag: 'Fleet Ready',
            tagColor: '#3b82f6',
            color: '#1e3a8a',
            bg: 'rgba(59, 130, 246, 0.08)',
        },
        {
            id: 'ev',
            name: 'EV Insurance',
            desc: 'Specialized coverage for Electric vehicles with battery protection add-ons.',
            tag: 'New Energy',
            tagColor: '#60a5fa',
            color: '#1d4ed8',
            bg: 'rgba(96, 165, 250, 0.08)',
        },
    ]);

    features = signal([
        {
            icon: 'shield',
            title: 'Full Protection',
            desc: 'Comprehensive coverage against accidents, theft, fire, and natural disasters.',
            color: '#312e81',
        },
        {
            icon: 'bolt',
            title: 'Digital-First',
            desc: '100% paperless process from quote to policy issuance within minutes.',
            color: '#4f46e5',
        },
        {
            icon: 'wrench',
            title: 'Priority Service',
            desc: 'Direct access to 5,000+ top-rated garages for quick and seamless repairs.',
            color: '#1d4ed8',
        },
        {
            icon: 'phone',
            title: 'Always On Support',
            desc: 'Round-the-clock dedicated helpline and live claim tracking on your dashboard.',
            color: '#3730a3',
        },
        {
            icon: 'star',
            title: 'Max Value',
            desc: 'Zero depreciation and hidden charge protection for your high-value assets.',
            color: '#1e3a8a',
        },
        {
            icon: 'clock',
            title: 'India-Wide Help',
            desc: 'On-spot roadside assistance including towing, fuel, and battery support.',
            color: '#4338ca',
        },
    ]);



    faqs = signal([
        {
            q: 'How quickly can I get insured?',
            a: 'Once you register and submit your vehicle application, our underwriting team reviews it within 24–48 hours. You get instant policy issuance upon approval.',
        },
        {
            q: 'What documents do I need to apply?',
            a: 'You need your vehicle registration certificate (RC), driving license, and previous insurance policy (if renewing). All documents are submitted digitally.',
        },
        {
            q: 'How do I file a claim?',
            a: 'Log into your customer portal, navigate to Claims, and submit your claim with photos and description. Our claims officer will be assigned within 2 hours.',
        },
        {
            q: 'Can I change my policy plan?',
            a: 'Yes, you can upgrade or modify your policy at renewal time. Our underwriting team processes changes within 1 business day.',
        },
        {
            q: 'Is roadside assistance available 24/7?',
            a: 'Absolutely! Our roadside assistance is available round-the-clock, 365 days a year across all major cities and highways in India.',
        },
    ]);

    @HostListener('window:scroll')
    onScroll() {
        this.isScrolled.set(window.scrollY > 60);
    }

    ngOnInit() { }

    toggleFaq(i: number) {
        this.activeFaq.set(this.activeFaq() === i ? null : i);
    }

    setTab(i: number) {
        this.activeTab.set(i);
    }
}
