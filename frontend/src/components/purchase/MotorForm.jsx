import { CURRENT_YEAR } from "../../pages/customer/purchase/constants";

export default function MotorForm({ value, onChange }) {
  const v = value || { regNo: "", make: "", model: "", year: "" };
  return (
    <div className="card">
      <div className="card-body">
        <div className="form-row">
          <div>
            <label className="form-label">Registration number</label>
            <input
              className="form-input"
              placeholder="e.g., MH12AB1234"
              value={v.regNo}
              onChange={(e) => onChange((p) => ({ ...p, regNo: e.target.value }))}
            />
          </div>
          <div>
            <label className="form-label">Make</label>
            <input
              className="form-input"
              placeholder="e.g., Maruti"
              value={v.make}
              onChange={(e) => onChange((p) => ({ ...p, make: e.target.value }))}
            />
          </div>
        </div>
        <div className="form-row">
          <div>
            <label className="form-label">Model</label>
            <input
              className="form-input"
              placeholder="e.g., Baleno"
              value={v.model}
              onChange={(e) => onChange((p) => ({ ...p, model: e.target.value }))}
            />
          </div>
          <div>
            <label className="form-label">Manufacture year</label>
            <input
              type="number"
              className="form-input"
              placeholder="YYYY"
              min={CURRENT_YEAR - 15}
              max={CURRENT_YEAR}
              value={v.year}
              onChange={(e) => onChange((p) => ({ ...p, year: e.target.value }))}
            />
            <span className="field-hint">Vehicles older than 15 years aren’t eligible.</span>
          </div>
        </div>
      </div>
    </div>
  );
}
