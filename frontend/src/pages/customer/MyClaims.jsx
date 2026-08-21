import { useState, useMemo } from "react";
import { Link } from "react-router-dom";
import { claimApi } from "../../api/claimApi";
import { useFetch } from "../../hooks/useFetch";
import Loader from "../../components/common/Loader";
import Alert from "../../components/common/Alert";
import Input from "../../components/common/Input";
import Pagination from "../../components/common/Pagination";
import EmptyState from "../../components/common/EmptyState";
import StatusBadge from "../../components/common/StatusBadge";
import { formatCurrency, formatDate } from "../../utils/formatters";

export default function MyClaims() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");

  const { data, loading, error } = useFetch(
    () => claimApi.getMyClaims({ page, size: 10 }),
    [page],
  );

  const includesCI = (haystack, needle) =>
    String(haystack || "").toLowerCase().includes(String(needle || "").toLowerCase());

  const filteredRows = useMemo(() => {
    const rows = data?.content || [];
    const q = (search || "").trim();
    if (!q) return rows;
    return rows.filter((c) =>
      includesCI(c.claimNumber, q) ||
      includesCI(c.policyNumber, q) ||
      includesCI(c.claimReason, q) ||
      includesCI(c.claimStatus, q)
    );
  }, [data, search]);

  if (loading) return <Loader label="Loading your claims..." />;

  return (
    <div>
      <div className="page-header">
        <h1>My Claims</h1>
        <p className="page-subtitle">
          Track the status of claims you've submitted
        </p>
      </div>

      <Alert type="error" message={error} />

      {data?.content?.length === 0 ? (
        <EmptyState message="You haven't filed any claims yet." />
      ) : (
        <div className="table-wrap">
          <div style={{ display: "flex", justifyContent: "flex-end", padding: "0.75rem" }}>
            <div style={{ minWidth: 220, maxWidth: 320 }}>
              <Input
                placeholder="Search by claim no, policy no, status…"
                value={search}
                onChange={(e) => {
                  setSearch(e.target.value);
                  setPage(0);
                }}
              />
            </div>
          </div>
          <table className="data-table">
            <thead>
              <tr>
                <th>Claim No.</th>
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
                  <td>{claim.policyNumber}</td>
                  <td>{formatCurrency(claim.claimAmount)}</td>
                  <td>{formatDate(claim.incidentDate)}</td>
                  <td>
                    <StatusBadge status={claim.claimStatus} />
                  </td>
                  <td>
                    <Link
                      className="link-btn"
                      to={`/customer/claims/${claim.claimId}`}
                    >
                      View
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
