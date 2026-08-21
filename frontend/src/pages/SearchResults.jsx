import { useEffect, useMemo, useState } from "react";
import { useLocation, Link } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { searchEverything } from "../api/searchService";
import Loader from "../components/common/Loader";
import EmptyState from "../components/common/EmptyState";

function useQuery() {
  const { search } = useLocation();
  return useMemo(() => new URLSearchParams(search), [search]);
}

export default function SearchResults() {
  const { user } = useAuth();
  const role = user?.role || "CUSTOMER";
  const qp = useQuery();
  const initial = qp.get("q") || "";
  const [q, setQ] = useState(initial);
  const [tab, setTab] = useState("POLICIES");
  const [loading, setLoading] = useState(false);
  const [res, setRes] = useState({ policies: [], claims: [] });

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      try {
        const r = await searchEverything(q, role);
        setRes(r);
      } finally {
        setLoading(false);
      }
    };
    if ((q || "").trim().length >= 2) run();
    else setRes({ policies: [], claims: [] });
  }, [q, role]);

  const Header = (
    <div className="page-header-row" style={{ alignItems: "center" }}>
      <div>
        <h1>Search</h1>
        <p className="page-subtitle">Find policies and claims quickly</p>
      </div>
      <input
        className="form-input"
        placeholder="Type to search…"
        value={q}
        onChange={(e) => setQ(e.target.value)}
        style={{ maxWidth: 360 }}
      />
    </div>
  );

  if (loading) return (<div className="app-content"><div className="page-header">{Header}</div><Loader label="Searching…" /></div>);

  return (
    <div className="app-content">
      <div className="page-header">{Header}</div>
      <div style={{ display: "flex", gap: 8, marginBottom: 12 }}>
        {[
          { k: "POLICIES", label: `Policies (${res.policies.length})` },
          { k: "CLAIMS", label: `Claims (${res.claims.length})` },
        ].map((t) => (
          <button
            key={t.k}
            type="button"
            className={`chip ${tab === t.k ? "selected" : ""}`}
            onClick={() => setTab(t.k)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === "POLICIES" ? (
        res.policies.length === 0 ? (
          <EmptyState message="No policies match your search." />
        ) : (
          <div className="table-wrap">
            <table className="data-table"><thead><tr><th>Policy</th><th>Plan</th><th>Status</th><th>Action</th></tr></thead><tbody>
              {res.policies.map((p) => (
                <tr key={`p-${p.id}`}>
                  <td>{p.number || p.id}</td>
                  <td>{p.title}</td>
                  <td>{p.status}</td>
                  <td>
                    {role === "CUSTOMER" ? (
                      <Link className="link-btn" to={`/customer/policies/${p.id}`}>Open</Link>
                    ) : role === "AGENT" ? (
                      <Link className="link-btn" to={`/agent/policies?policyId=${p.id}`}>Open</Link>
                    ) : (
                      <Link className="link-btn" to={`/admin/policies?policyId=${p.id}`}>Open</Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody></table>
          </div>
        )
      ) : (
        res.claims.length === 0 ? (
          <EmptyState message="No claims match your search." />
        ) : (
          <div className="table-wrap">
            <table className="data-table"><thead><tr><th>Claim</th><th>Title</th><th>Status</th><th>Action</th></tr></thead><tbody>
              {res.claims.map((c) => (
                <tr key={`c-${c.id}`}>
                  <td>{c.number || c.id}</td>
                  <td>{c.title}</td>
                  <td>{c.status}</td>
                  <td>
                    {role === "CUSTOMER" ? (
                      <Link className="link-btn" to={`/customer/claims/${c.id}`}>Open</Link>
                    ) : role === "AGENT" ? (
                      <Link className="link-btn" to={`/agent/claims/${c.id}`}>Open</Link>
                    ) : (
                      <Link className="link-btn" to={`/admin/claims/${c.id}`}>Open</Link>
                    )}
                  </td>
                </tr>
              ))}
            </tbody></table>
          </div>
        )
      )}
    </div>
  );
}
