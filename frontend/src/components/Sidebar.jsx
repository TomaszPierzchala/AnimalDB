import {
  VIEW_GENE,
  VIEW_STRAIN
} from '../viewNames';

import './Sidebar.css';

const navigationItems = [
  {
    id: VIEW_GENE,
    label: 'Genes'
  },
  {
    id: VIEW_STRAIN,
    label: 'Strains'
  }
];

function Sidebar({
  activeView,
  onViewChange
}) {
  return (
    <nav
      className="sidebar-navigation"
      aria-label="AnimalDB views"
    >
      {navigationItems.map((item) => (
        <button
          key={item.id}
          type="button"
          className={
            activeView === item.id
              ? 'sidebar-link sidebar-link-active'
              : 'sidebar-link'
          }
          onClick={() => onViewChange(item.id)}
        >
          {item.label}
        </button>
      ))}
    </nav>
  );
}

export default Sidebar;
