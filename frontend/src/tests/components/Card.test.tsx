import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Card } from '../../components/ui/Card';

describe('Card Component', () => {
  it('renders card with children', () => {
    render(
      <Card>
        <div>Card Content</div>
      </Card>
    );
    expect(screen.getByText('Card Content')).toBeInTheDocument();
  });

  it('applies glass variant', () => {
    const { container } = render(<Card glass>Content</Card>);
    const card = container.firstChild;
    expect(card).toHaveClass('backdrop-blur-sm');
  });

  it('renders CardHeader', () => {
    const { CardHeader } = Card as any;
    render(
      <Card>
        <CardHeader>Header Text</CardHeader>
      </Card>
    );
    expect(screen.getByText('Header Text')).toBeInTheDocument();
  });
});
