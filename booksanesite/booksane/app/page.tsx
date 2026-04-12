import Navbar from '@/components/landing/Navbar';
import Hero from '@/components/landing/Hero';
import Marquee from '@/components/landing/Marquee';
import HowItWorks from '@/components/landing/HowItWorks';
import TemplateShowcase from '@/components/landing/TemplateShowcase';
import Features from '@/components/landing/Features';
import Testimonials from '@/components/landing/Testimonials';
import BookShowcase from '@/components/landing/BookShowcase';
import Footer from '@/components/landing/Footer';

export default function HomePage() {
  return (
    <main className="min-h-screen overflow-x-hidden" style={{ background: '#0A0910' }}>
      <Navbar />
      <Hero />
      <Marquee />
      <HowItWorks />
      <TemplateShowcase />
      <Features />
      <Testimonials />
      <BookShowcase />
      <Footer />
    </main>
  );
}
