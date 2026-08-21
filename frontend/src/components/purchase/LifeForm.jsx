import { TODAY_STR } from "../../utils/dates";

export default function LifeForm({ value, onChange, prefilledNote = false }) {
  const v = value || {
    dob: "",
    smoker: "",
    nominees: [{ name: "", relationship: "SPOUSE", sharePct: 100, dob: "" }],
    consentTruth: false,
    consentTnc: false,
  };

  const nominee = v.nominees?.[0] || { name: "", relationship: "SPOUSE", sharePct: 100, dob: "" };

  return (
    <div className="card">
      <div className="card-body">
        <div className="form-row">
          <input
            type="date"
            className="form-input"
            value={v.dob}
            max={TODAY_STR}
            onChange={(e) => onChange((p) => ({ ...p, dob: e.target.value }))}
          />
          <select
            className="form-input"
            value={v.smoker}
            onChange={(e) => onChange((p) => ({ ...p, smoker: e.target.value }))}
          >
            <option value="">Smoker?</option>
            <option value="false">No</option>
            <option value="true">Yes</option>
          </select>
        </div>
        {/* Occupation Risk removed by business decision */}
        <div>
          <label className="figure-label">Nominee</label>
          {prefilledNote && (
            <p className="help" style={{ marginTop: "0.25rem" }}>
              Using your profile nominee. You can edit it here or update permanently in your profile.
            </p>
          )}
          <div className="plan-field-row">
            <input
              className="form-input"
              placeholder="Name"
              value={nominee.name || ""}
              onChange={(e) =>
                onChange((p) => ({ ...p, nominees: [{ ...p.nominees?.[0], name: e.target.value || "" }] }))
              }
            />
            <select
              className="form-input"
              value={nominee.relationship || "SPOUSE"}
              onChange={(e) => onChange((p) => ({ ...p, nominees: [{ ...p.nominees?.[0], relationship: e.target.value }] }))}
            >
              <option value="SPOUSE">Spouse</option>
              <option value="PARENT">Parent</option>
              <option value="CHILD">Child</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="plan-field-inline-2" style={{ marginTop: "0.5rem" }}>
            <input
              type="number"
              className="form-input"
              placeholder="Share %"
              min={100}
              max={100}
              value={nominee.sharePct ?? 100}
              disabled
              readOnly
            />
            <input
              type="date"
              className="form-input"
              placeholder="Nominee DOB (optional)"
              max={TODAY_STR}
              value={nominee.dob || ""}
              onChange={(e) => onChange((p) => ({ ...p, nominees: [{ ...p.nominees?.[0], dob: e.target.value }] }))}
            />
          </div>
          <div className="plan-field-inline-2" style={{ marginTop: "0.5rem" }}>
            <label style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
              <input
                type="checkbox"
                checked={Boolean(v.consentTruth)}
                onChange={(e) => onChange((p) => ({ ...p, consentTruth: e.target.checked }))}
              />
              <span className="field-hint">I confirm details are true</span>
            </label>
            <label style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
              <input
                type="checkbox"
                checked={Boolean(v.consentTnc)}
                onChange={(e) => onChange((p) => ({ ...p, consentTnc: e.target.checked }))}
              />
              <span className="field-hint">Agree to Terms & Conditions</span>
            </label>
          </div>
        </div>
      </div>
    </div>
  );
}
