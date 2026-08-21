import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { claimApi } from "../../api/claimApi";
import { useFetch } from "../../hooks/useFetch";
import Loader from "../../components/common/Loader";
import Alert from "../../components/common/Alert";
import Select from "../../components/common/Select";
import Input from "../../components/common/Input";
import Pagination from "../../components/common/Pagination";
import EmptyState from "../../components/common/EmptyState";
import StatusBadge from "../../components/common/StatusBadge";
import { formatCurrency, formatDate } from "../../utils/formatters";
import { CLAIM_STATUSES } from "../../utils/constants";
import { exportToCsv } from "../../utils/exportCsv";

export default function AgentClaims() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [search, setSearch] = useState("");

  const { data, loading, error } = useFetch(
    () =>
      claimApi.getAssignedToMe({
        page,
        size: 10,
        status: statusFilter || undefined,
      }),
    [page, statusFilter],
  );

  const handleFilterChange = (e) => {
    setStatusFilter(e.target.value);
    setPage(0);
  };

  const claims = data?.content || [];
  const includesCI = (h, n) => String(h || "").toLowerCase().includes(String(n || "").toLowerCase());
  const filteredRows = useMemo(() => {
    const q = (search || "").trim();
    if (!q) return claims;
    return claims.filter((c) =>
      includesCI(c.claimNumber, q) ||
      includesCI(c.policyNumber, q) ||
      includesCI(c.claimStatus, q) ||
      includesCI(c.claimReason, q) ||
      includesCI(c.customerName, q)
    );
  }, [claims, search]);
  if (loading) return <Loader label="Loading claims..." />;
  const reviewQueue = claims.filter((claim) =>
    ["SUBMITTED", "UNDER_REVIEW"].includes(claim.claimStatus),
  ).length;

  return (
    <div className="ops-page">
      <div className="ops-hero claims-hero agent-claims-hero">
        <div>
          <span className="eyebrow">Officer Claim Queue</span>
          <h1>Review Claims</h1>
          <p>
            Work through assigned claims with quick filtering, evidence context,
            and review actions.
          </p>
        </div>
        <div className="ops-hero-panel">
          <strong>{reviewQueue}</strong>
          <span>need review</span>
          <p>{claims.length} assigned claims visible</p>
        </div>
      </div>

      <Alert type="error" message={error} />

      <div className="ops-toolbar split-toolbar">
        <div className="filter-bar" style={{ display: "flex", gap: 12, alignItems: "center", flexWrap: "wrap" }}>
          <Select
            name="status"
            value={statusFilter}
            onChange={handleFilterChange}
            options={CLAIM_STATUSES}
            placeholder="All statuses"
          />
          <div style={{ minWidth: 220, maxWidth: 320 }}>
            <Input
              placeholder="Search claim no, policy, status…"
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
            />
          </div>
        </div>
        <button
          className="btn btn-secondary"
          onClick={() =>
            exportToCsv("assigned-claims", data?.content || [], [
              { header: "Claim No", value: (c) => c.claimNumber },
              { header: "Customer", value: (c) => c.customerName },
              { header: "Policy", value: (c) => c.policyNumber },
              { header: "Amount", value: (c) => c.claimAmount },
              { header: "Incident Date", value: (c) => c.incidentDate },
              { header: "Status", value: (c) => c.claimStatus },
            ])
          }
        >
          Export CSV
        </button>
      </div>

      {claims.length === 0 ? (
        <EmptyState message="No claims match this filter." />
      ) : (
        <div className="table-wrap ops-table">
          <table className="data-table">
            <thead>
              <tr>
                <th>Claim No.</th>
                <th>Customer</th>
                <th>Policy</th>
                <th>Amount</th>
                <th>Incident Date</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.map((claim) => (
                <tr key={claim.claimId}>
                  <td>{claim.claimNumber}</td>
                  <td>{claim.customerName}</td>
                  <td>{claim.policyNumber}</td>
                  <td>{formatCurrency(claim.claimAmount)}</td>
                  <td>{formatDate(claim.incidentDate)}</td>
                  <td>
                    <StatusBadge status={claim.claimStatus} />
                  </td>
                  <td>
                    <Link
                      className="link-btn"
                      to={`/agent/claims/${claim.claimId}`}
                    >
                      Review
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Pagination pageData={data} onPageChange={setPage} />
    </div>
  );
}
