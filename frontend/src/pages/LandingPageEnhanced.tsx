import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Zap, Users, BarChart3, Shield, CheckCircle2, FolderKanban, Clock, Linkedin } from 'lucide-react';
import { Button } from '../components/ui/Button';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-primary-900 to-slate-900">
      {/* Navigation */}
      <nav className="flex items-center justify-between px-6 py-4 sm:px-12 border-b border-white/10">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-xl">
            ⚡
          </div>
          <div>
            <h1 className="text-xl font-bold text-white">Elara</h1>
            <p className="text-xs text-primary-300">Elara</p>
          </div>
        </div>
        <div className="flex gap-3">
          <Button 
            onClick={() => navigate('/login')}
            variant="secondary"
            size="sm"
          >
            Sign In
          </Button>
        </div>
      </nav>

      {/* Hero Section */}
      <div className="flex items-center justify-center px-6 py-16 sm:px-12 sm:py-24">
        <div className="max-w-3xl text-center space-y-8">
          <div className="space-y-4">
            <div className="inline-block px-4 py-2 rounded-full bg-primary-500/20 border border-primary-500/50">
              <p className="text-sm text-primary-200">🚀 Welcome to Elara</p>
            </div>
            
            <h1 className="text-5xl sm:text-7xl font-bold text-white leading-tight">
              Manage Your Team's Workflow<br />
              <span className="bg-gradient-to-r from-primary-400 to-secondary-400 bg-clip-text text-transparent">
                with Powerful Tools
              </span>
            </h1>
            
            <p className="text-xl text-gray-300 max-w-2xl mx-auto">
              Streamline project management, track epics and stories, collaborate with your team, and deliver projects on time. Built for enterprise teams that demand excellence.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-center pt-4">
            <Button 
              onClick={() => navigate('/login')}
              variant="primary"
              className="px-8 py-4 text-lg"
              icon={<ArrowRight className="w-5 h-5" />}
            >
              Get Started Now
            </Button>
            <a href="#features" className="px-8 py-4 bg-slate-700/50 hover:bg-slate-600/50 text-white rounded-lg font-semibold transition-colors border border-slate-600 flex items-center justify-center gap-2">
              Explore Features ↓
            </a>
          </div>

          {/* Stats */}
          <div className="grid grid-cols-3 gap-4 pt-8 border-t border-white/10">
            <div>
              <p className="text-2xl font-bold text-primary-400">100%</p>
              <p className="text-sm text-gray-400">Uptime</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-primary-400">∞</p>
              <p className="text-sm text-gray-400">Scalability</p>
            </div>
            <div>
              <p className="text-2xl font-bold text-primary-400">24/7</p>
              <p className="text-sm text-gray-400">Support</p>
            </div>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div id="features" className="bg-slate-800/30 px-6 py-16 sm:px-12 border-t border-white/10">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-white mb-4">
              Powerful Features
            </h2>
            <p className="text-gray-400 text-lg">
              Everything you need to manage workflows effectively
            </p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              {
                icon: <Zap className="w-8 h-8" />,
                title: "Lightning Fast",
                description: "Real-time updates and instant notifications keep your team in sync"
              },
              {
                icon: <FolderKanban className="w-8 h-8" />,
                title: "Project Management",
                description: "Organize projects, track epics, and manage stories efficiently"
              },
              {
                icon: <Users className="w-8 h-8" />,
                title: "Team Collaboration",
                description: "Assign tasks, track progress, and collaborate seamlessly"
              },
              {
                icon: <BarChart3 className="w-8 h-8" />,
                title: "Analytics & Reports",
                description: "Generate comprehensive reports in Excel, Word, or PDF formats"
              },
              {
                icon: <Shield className="w-8 h-8" />,
                title: "Role-Based Access",
                description: "Enterprise-grade security with fine-grained access control"
              },
              {
                icon: <Clock className="w-8 h-8" />,
                title: "SLA Management",
                description: "Define and monitor SLA rules to ensure timely delivery"
              },
            ].map((feature, i) => (
              <div key={i} className="bg-slate-700/30 rounded-xl p-6 space-y-3 border border-slate-600/50 hover:border-primary-500/50 hover:bg-slate-700/50 transition-all group">
                <div className="text-primary-400 group-hover:text-primary-300 transition-colors">
                  {feature.icon}
                </div>
                <h3 className="text-lg font-semibold text-white">
                  {feature.title}
                </h3>
                <p className="text-gray-400 text-sm">
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Key Benefits */}
      <div className="px-6 py-16 sm:px-12">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-3xl font-bold text-white mb-12 text-center">
            Why Choose Elara?
          </h2>
          
          <div className="space-y-4">
            {[
              "✓ Intuitive interface - No learning curve",
              "✓ Multi-level access control - Secure your data",
              "✓ Real-time notifications - Stay informed",
              "✓ Advanced reporting - Make data-driven decisions",
              "✓ Team management - Organize your workforce",
              "✓ CI/CD Integration - Streamline your workflow",
            ].map((benefit, i) => (
              <div key={i} className="flex items-center gap-3 text-gray-300 bg-slate-700/20 px-6 py-3 rounded-lg border border-slate-600/30 hover:border-primary-500/30 transition-colors">
                <CheckCircle2 className="w-5 h-5 text-primary-400 flex-shrink-0" />
                <p className="text-lg">{benefit}</p>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Founder Section */}
      <div className="bg-gradient-to-r from-primary-900/40 to-secondary-900/40 px-6 py-16 sm:px-12 border-y border-white/10">
        <div className="max-w-2xl mx-auto text-center">
          <h3 className="text-2xl font-bold text-white mb-4">Built by Enterprise Team</h3>
          <p className="text-gray-300 mb-6">
            Developed by passionate engineers who understand the complexities of workflow management. Elara's mission is to simplify project management for teams of all sizes.
          </p>
          <div className="flex items-center justify-center gap-4">
            <a 
              href="https://www.linkedin.com/" 
              target="_blank" 
              rel="noreferrer"
              className="inline-flex items-center gap-2 px-6 py-3 bg-primary-600 hover:bg-primary-700 text-white rounded-lg font-semibold transition-colors"
            >
              <Linkedin className="w-5 h-5" />
              Connect with Founder
            </a>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="px-6 py-16 sm:px-12">
        <div className="max-w-2xl mx-auto text-center bg-gradient-to-r from-primary-500/20 to-secondary-500/20 rounded-xl border border-primary-500/30 p-8 space-y-6">
          <h2 className="text-3xl font-bold text-white">
            Ready to Transform Your Workflow?
          </h2>
          <p className="text-gray-300 text-lg">
            Start managing your enterprise projects smarter, faster, and more collaboratively today.
          </p>
          <Button 
            onClick={() => navigate('/login')}
            variant="primary"
            className="px-8 py-4"
          >
            Get Started Now
          </Button>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-white/10 px-6 py-8 sm:px-12">
        <div className="max-w-6xl mx-auto">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 mb-8">
            <div>
              <h4 className="text-white font-semibold mb-4">Product</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li><a href="#" className="hover:text-white transition">Features</a></li>
                <li><a href="#" className="hover:text-white transition">Pricing</a></li>
                <li><a href="#" className="hover:text-white transition">Security</a></li>
              </ul>
            </div>
            <div>
              <h4 className="text-white font-semibold mb-4">Company</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li><a href="https://www.linkedin.com/" target="_blank" rel="noreferrer" className="hover:text-white transition">About Us</a></li>
                <li><a href="#" className="hover:text-white transition">Blog</a></li>
                <li><a href="#" className="hover:text-white transition">Careers</a></li>
              </ul>
            </div>
            <div>
              <h4 className="text-white font-semibold mb-4">Legal</h4>
              <ul className="space-y-2 text-gray-400 text-sm">
                <li><a href="#" className="hover:text-white transition">Privacy</a></li>
                <li><a href="#" className="hover:text-white transition">Terms</a></li>
                <li><a href="#" className="hover:text-white transition">Contact</a></li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-white/10 pt-8 flex items-center justify-between">
            <p className="text-gray-500 text-sm">
              © 2026 Elara. All rights reserved.
            </p>
            <div className="flex gap-4">
              <a href="https://www.linkedin.com/" target="_blank" rel="noreferrer" className="text-gray-400 hover:text-white">
                <Linkedin className="w-5 h-5" />
              </a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
