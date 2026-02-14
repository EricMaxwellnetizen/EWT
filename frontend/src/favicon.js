/* Logo SVG stored as base64 - Tech company logo with modern design */
window.addEventListener('DOMContentLoaded', () => {
  // Create favicon SVG
  const svgData = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="64" height="64">
      <defs>
        <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#6366f1;stop-opacity:1" />
          <stop offset="100%" style="stop-color:#8b5cf6;stop-opacity:1" />
        </linearGradient>
      </defs>
      
      <!-- Background circle -->
      <circle cx="50" cy="50" r="50" fill="url(#grad)"/>
      
      <!-- Modern tech symbol - workflow -->
      <g fill="#ffffff">
        <!-- Top node -->
        <circle cx="50" cy="20" r="8"/>
        
        <!-- Middle nodes -->
        <circle cx="30" cy="50" r="8"/>
        <circle cx="50" cy="50" r="8"/>
        <circle cx="70" cy="50" r="8"/>
        
        <!-- Bottom nodes -->
        <circle cx="40" cy="75" r="8"/>
        <circle cx="60" cy="75" r="8"/>
        
        <!-- Connecting lines -->
        <line x1="50" y1="28" x2="30" y2="42" stroke="#ffffff" stroke-width="2"/>
        <line x1="50" y1="28" x2="50" y2="42" stroke="#ffffff" stroke-width="2"/>
        <line x1="50" y1="28" x2="70" y2="42" stroke="#ffffff" stroke-width="2"/>
        
        <line x1="30" y1="58" x2="40" y2="67" stroke="#ffffff" stroke-width="2"/>
        <line x1="50" y1="58" x2="40" y2="67" stroke="#ffffff" stroke-width="2"/>
        <line x1="50" y1="58" x2="60" y2="67" stroke="#ffffff" stroke-width="2"/>
        <line x1="70" y1="58" x2="60" y2="67" stroke="#ffffff" stroke-width="2"/>
      </g>
    </svg>
  `;

  // Convert to base64
  const link = document.createElement('link');
  link.rel = 'icon';
  link.type = 'image/svg+xml';
  link.href = 'data:image/svg+xml;base64,' + btoa(svgData);
  document.head.appendChild(link);
});
