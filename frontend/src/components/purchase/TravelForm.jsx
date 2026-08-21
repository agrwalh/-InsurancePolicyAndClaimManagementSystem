import { TODAY_STR } from "../../utils/dates";
import { clampInt } from "../../utils/validators";

export default function TravelForm({ value, onChange }) {
  const v = value || { start: TODAY_STR, end: "", adults: "", children: "" };
  return (
    <div className="card">
      <div className="card-body">
        <div className="form-row">
          <div>
            <label className="form-label">Trip start date</label>
            <input
              type="date"
              className="form-input"
              value={v.start}
              min={TODAY_STR}
              onChange={(e) => onChange((p) => ({ ...p, start: e.target.value }))}
            />
          </div>
          <div>
            <label className="form-label">Trip end date</label>
            <input
              type="date"
              className="form-input"
              value={v.end}
              min={v.start || TODAY_STR}
              onChange={(e) => onChange((p) => ({ ...p, end: e.target.value }))}
            />
          </div>
        </div>
        <div className="form-row">
          <div>
            <label className="form-label">Adults</label>
            <input
              type="number"
              className="form-input"
              placeholder="1"
              min={1}
              max={9}
              value={v.adults}
              onChange={(e) => onChange((p) => ({ ...p, adults: clampInt(e.target.value, 1, 9) }))}
            />
          </div>
          <div>
            <label className="form-label">Children</label>
            <input
              type="number"
              className="form-input"
              placeholder="0"
              min={0}
              max={9}
              value={v.children}
              onChange={(e) => onChange((p) => ({ ...p, children: clampInt(e.target.value, 0, 9) }))}
            />
          </div>
        </div>
        <p className="field-hint">Trip duration must be between 1 and 180 days.</p>
      </div>
    </div>
  );
}
