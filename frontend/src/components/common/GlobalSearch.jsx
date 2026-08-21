import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { searchEverything } from "../../api/searchService";

export default function GlobalSearch() {
  const { user } = useAuth();
  const role = user?.role || "CUSTOMER";
  const navigate = useNavigate();
  const [q, setQ] = useState("");
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState({ policies: [], claims: [] });
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef(null);
  const abortRef = useRef({ aborted: false });

  // Keyboard shortcut: Cmd/Ctrl+K
  useEffect(() => {
    const onKey = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        inputRef.current?.focus();
        setOpen(true);
      }
      if (e.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  const flat = useMemo(() => {
    const out = [];
    results.policies.forEach((p) => out.push({ ...p, __group: "Policies" }));
    results.claims.forEach((c) => out.push({ ...c, __group: "Claims" }));
    return out;
  }, [results]);

  // Debounced search
  useEffect(() => {
    const qTrim = q.trim();
    if (qTrim.length < 2) {
      setResults({ policies: [], claims: [] });
      return;
    }
    setLoading(true);
    const token = { cancelled: false };
    const h = setTimeout(async () => {
      try {
        const r = await searchEverything(qTrim, role);
        if (!token.cancelled) setResults(r);
      } catch (e) {
        if (!token.cancelled) setResults({ policies: [], claims: [] });
      } finally {
        if (!token.cancelled) setLoading(false);
      }
    }, 300);
    return () => {
      token.cancelled = true;
      clearTimeout(h);
    };
  }, [q, role]);

  const goTo = (item) => {
    if (!item) return;
    setOpen(false);
    if (item.type === "policy") {
      if (role === "CUSTOMER") navigate(`/customer/policies/${item.id}`);
      else if (role === "AGENT") navigate(`/agent/policies` + `?policyId=${item.id}`);
      else navigate(`/admin/policies` + `?policyId=${item.id}`);
    } else if (item.type === "claim") {
      if (role === "CUSTOMER") navigate(`/customer/claims/${item.id}`);
      else if (role === "AGENT") navigate(`/agent/claims/${item.id}`);
      else navigate(`/admin/claims/${item.id}`);
    }
  };

  const onKeyDown = (e) => {
    if (!open) return;
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex((i) => Math.min(i + 1, flat.length - 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex((i) => Math.max(i - 1, 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (flat[activeIndex]) goTo(flat[activeIndex]);
      else navigate(`/search?q=${encodeURIComponent(q.trim())}`);
    } else if (e.key === "Escape") {
      setOpen(false);
    }
  };

  return (
    <div className="global-search" style={{ position: "relative", minWidth: 260 }}>
      <input
        ref={inputRef}
        className="form-input"
        placeholder="Search policy or claim (Ctrl/Cmd+K)"
        value={q}
        onChange={(e) => {
          setQ(e.target.value);
          setOpen(true);
          setActiveIndex(0);
        }}
        onFocus={() => setOpen(true)}
        onKeyDown={onKeyDown}
      />
      {open && (q.trim().length >= 2) && (
        <div
          className="search-dropdown"
          style={{
            position: "absolute",
            top: "100%",
            left: 0,
            right: 0,
            background: "var(--surface)",
            border: "1px solid var(--border)",
            borderRadius: 10,
            boxShadow: "var(--sh-2)",
            zIndex: 30,
            marginTop: 6,
            padding: 6,
          }}
        >
          {loading && <div className="field-hint" style={{ padding: 8 }}>Searching…</div>}
          {!loading && flat.length === 0 && (
            <div className="field-hint" style={{ padding: 8 }}>No quick matches. Press Enter to view all results.</div>
          )}
          {!loading && flat.length > 0 && (
            <ul style={{ listStyle: "none", margin: 0, padding: 0, maxHeight: 260, overflowY: "auto" }}>
              {flat.map((item, idx) => (
                <li
                  key={`${item.type}-${item.id}`}
                  onMouseDown={(e) => e.preventDefault()}
                  onClick={() => goTo(item)}
                  className={idx === activeIndex ? "active" : ""}
                  style={{
                    padding: "8px 10px",
                    borderRadius: 8,
                    background: idx === activeIndex ? "var(--surface-2)" : "transparent",
                    cursor: "pointer",
                    display: "grid",
                    gridTemplateColumns: "1fr auto",
                    gap: 8,
                    alignItems: "center",
                  }}
                >
                  <div>
                    <div style={{ fontWeight: 700, fontSize: "0.9rem" }}>{item.number || item.title}</div>
                    <div className="field-hint" style={{ fontSize: "0.78rem" }}>{item.title} • {item.subtitle}</div>
                  </div>
                  <span className={`badge ${item.type === 'policy' ? 'badge-blue' : 'badge-purple'}`}>
                    {item.type}
                  </span>
                </li>
              ))}
              <li
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => navigate(`/search?q=${encodeURIComponent(q.trim())}`)}
                style={{ padding: "8px 10px", borderTop: "1px solid var(--border)", marginTop: 6, cursor: "pointer" }}
              >
                View all results for “{q.trim()}” →
              </li>
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
