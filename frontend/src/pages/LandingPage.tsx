import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowRight, Zap, Users, BarChart3, Shield } from 'lucide-react';
import { Button } from '../components/ui/Button';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-primary-900 to-slate-900">
      {/* Navigation */}
      <nav className="flex items-center justify-between px-6 py-4 sm:px-12">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold">
            EW
          </div>
          <span className="text-xl font-bold text-white">Elara</span>
        </div>
        <Button 
          onClick={() => navigate('/login')}
          variant="primary"
        >
          Sign In
        </Button>
      </nav>

      {/* Hero Section */}
      <div className="flex items-center justify-center px-6 py-20 sm:px-12 sm:py-32">
        <div className="max-w-2xl text-center space-y-8">
          <div className="space-y-4">
            <h1 className="text-4xl sm:text-6xl font-bold text-white leading-tight">
              Streamline Your Workflow
            </h1>
            <p className="text-xl text-gray-300">
              Manage projects, epics, stories, and teams with Elara's powerful workflow system
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button 
              onClick={() => navigate('/login')}
              variant="primary"
              className="px-8 py-3 text-lg"
              icon={<ArrowRight className="w-5 h-5" />}
            >
              Get Started
            </Button>
            <Button 
              variant="secondary"
              className="px-8 py-3 text-lg"
            >
              Learn More
            </Button>
          </div>
        </div>
      </div>

      {/* Features Section */}
      <div className="bg-slate-800/50 px-6 py-16 sm:px-12">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl font-bold text-white mb-12 text-center">
            Powerful Features
          </h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: <Zap className="w-8 h-8" />,
                title: "Fast & Responsive",
                description: "Lightning-quick performance for seamless collaboration"
              },
              {
                icon: <Users className="w-8 h-8" />,
                title: "Team Management",
                description: "Organize teams with role-based access control"
              },
              {
                icon: <BarChart3 className="w-8 h-8" />,
                title: "Analytics & Reports",
                description: "Track progress with comprehensive reporting tools"
              },
              {
                icon: <Shield className="w-8 h-8" />,
                title: "Secure & Reliable",
                description: "Enterprise-grade security with JWT authentication"
              },
            ].map((feature, i) => (
              <div key={i} className="bg-slate-700/50 rounded-lg p-6 space-y-3 border border-slate-600/50 hover:border-primary-500/50 transition-colors">
                <div className="text-primary-400">
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

      {/* CTA Section */}
      <div className="px-6 py-16 sm:px-12 text-center space-y-6">
        <h2 className="text-3xl font-bold text-white">
          Ready to transform your workflow?
        </h2>
        <Button 
          onClick={() => navigate('/login')}
          variant="primary"
          className="px-8 py-3 text-lg mx-auto"
        >
          Sign In Now
        </Button>
      </div>

      {/* Footer */}
      <div className="border-t border-slate-700/50 px-6 py-8 sm:px-12 text-center text-gray-400 text-sm">
        <p>© 2026 Elara. All rights reserved.</p>
      </div>
    </div>
  );
};
