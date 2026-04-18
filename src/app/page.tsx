import Navbar from '@/components/landing/Navbar';
import Hero from '@/components/landing/Hero';
import Marquee from '@/components/landing/Marquee';
import HowItWorks from '@/components/landing/HowItWorks';
import TemplateShowcase from '@/components/landing/TemplateShowcase';
import Features from '@/components/landing/Features';
import Testimonials from '@/components/landing/Testimonials';
import BookShowcase from '@/components/landing/BookShowcase';
import Footer from '@/components/landing/Footer';
import HorizontalGallery from '@/components/landing/HorizontalGallery';
import Reveal from '@/components/Reveal';

export default function HomePage() {
  return (
    <main className="min-h-screen overflow-x-hidden" style={{ background: '#0A0910' }}>
      <Navbar />
      <Hero />
      <Reveal y={30} duration={0.9}>
        <Marquee />
      </Reveal>
      <Reveal y={60} duration={0.95}>
        <HowItWorks />
      </Reveal>
      <Reveal y={60} duration={0.95}>
        <TemplateShowcase />
      </Reveal>
      <HorizontalGallery />
      <Reveal y={60} duration={0.95}>
        <Features />
      </Reveal>
      <Reveal y={60} duration={0.95}>
        <Testimonials />
      </Reveal>
      <Reveal y={60} duration={0.95}>
        <BookShowcase />
      </Reveal>
      <Reveal y={30} duration={0.85}>
        <Footer />
      </Reveal>
    </main>
  );
}
