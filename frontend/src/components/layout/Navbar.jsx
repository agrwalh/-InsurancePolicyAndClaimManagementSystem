import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useTheme } from "../../context/ThemeContext";
// no local state needed now
// Global search removed from navbar; page-level search is used instead

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { theme, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <>
      <header className="navbar">
        <div className="navbar-brand">
          <div className="brand-mark">SC</div>
          <span className="brand-name">SecureCover</span>
        </div>

        <div className="navbar-right">
          {/* Theme Toggle */}
          <button
            className="theme-toggle"
            onClick={toggleTheme}
            aria-label={`Switch to ${
              theme === "light" ? "dark" : "light"
            } mode`}
            title={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
          >
            {theme === "light" ? "🌙" : "☀️"}
          </button>


          {user && (
            <>
              <div className="navbar-user">
                <span className="user-name">{user.fullName}</span>
                <span className="user-role">
                  {user.role === "AGENT"
                    ? "Insurance Operations Officer"
                    : user.role}
                </span>
              </div>

              <button className="logout-btn" onClick={handleLogout}>
                Logout
              </button>
            </>
          )}
        </div>
      </header>

      {/* Officer modal removed intentionally */}
    </>
  );
}
