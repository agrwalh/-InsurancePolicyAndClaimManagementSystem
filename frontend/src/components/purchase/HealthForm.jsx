import ChipToggle from "../common/ChipToggle";
import { ensureLength, clampInt } from "../../utils/validators";
import { HEALTH_CONDITION_OPTIONS, CURRENT_YEAR } from "../../pages/customer/purchase/constants";

export default function HealthForm({ value, onChange }) {
  const v = value || {
    coverType: "INDIVIDUAL",
    insuredAge: "",
    adultCount: "",
    childCount: "",
    adultAges: [],
    childAges: [],
    preConds: [],
    preCondsSince: {},
  };

  return (
    <div className="card">
      <div className="card-body">
        <div className="form-row">
          <div>
            <label className="form-label">Cover type</label>
            <select
              className="form-input"
              value={v.coverType}
              onChange={(e) => onChange((p) => ({ ...p, coverType: e.target.value }))}
            >
              <option value="INDIVIDUAL">Individual</option>
              <option value="FLOATER">Family Floater</option>
            </select>
          </div>
          <div>
            <label className="form-label">Pre-existing conditions</label>
            <div className="chip-row">
              {HEALTH_CONDITION_OPTIONS.map((code) => {
                const checked = (v.preConds || []).includes(code);
                return (
                  <ChipToggle
                    key={code}
                    selected={checked}
                    onToggle={() => {
                      const set = new Set(v.preConds || []);
                      if (checked) set.delete(code); else set.add(code);
                      onChange((p) => ({ ...p, preConds: Array.from(set) }));
                    }}
                  >
                    {code.replace("_", "/")}
                  </ChipToggle>
                );
              })}
            </div>
          </div>
        </div>

        {(v.preConds || []).length > 0 && (
          <div className="form-row">
            {(v.preConds || []).map((code) => (
              <div key={`since-${code}`}>
                <label className="form-label">{code.replace("_", "/")} — Since (year)</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="YYYY"
                  min={CURRENT_YEAR - 80}
                  max={CURRENT_YEAR}
                  value={v.preCondsSince?.[code] ?? ""}
                  onChange={(e) => onChange((p) => ({
                    ...p,
                    preCondsSince: {
                      ...(p.preCondsSince || {}),
                      [code]: e.target.value === "" ? "" : clampInt(e.target.value, CURRENT_YEAR - 80, CURRENT_YEAR),
                    },
                  }))}
                />
              </div>
            ))}
          </div>
        )}

        {v.coverType === "INDIVIDUAL" ? (
          <div>
            <label className="form-label">Insured age</label>
            <input
              type="number"
              className="form-input"
              min={18}
              max={65}
              value={v.insuredAge}
              onChange={(e) => onChange((p) => ({ ...p, insuredAge: clampInt(e.target.value, 18, 65) }))}
            />
            <span className="field-hint">Eligible age range: 18–65 years.</span>
          </div>
        ) : (
          <>
            <div className="form-row">
              <div>
                <label className="form-label">Adults</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="1"
                  min={1}
                  max={2}
                  value={v.adultCount}
                  onChange={(e) =>
                    onChange((p) => ({
                      ...p,
                      adultCount: clampInt(e.target.value, 1, 2),
                      adultAges: ensureLength(p.adultAges, clampInt(e.target.value, 1, 2)),
                    }))
                  }
                />
              </div>
              <div>
                <label className="form-label">Children</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="0"
                  min={0}
                  max={4}
                  value={v.childCount}
                  onChange={(e) =>
                    onChange((p) => ({
                      ...p,
                      childCount: clampInt(e.target.value, 0, 4),
                      childAges: ensureLength(p.childAges, clampInt(e.target.value, 0, 4)),
                    }))
                  }
                />
              </div>
            </div>
            {Number(v.adultCount || 0) > 0 && (
              <div>
                <label className="form-label">Adult ages</label>
                <div className="plan-field-inline-2">
                  {ensureLength(v.adultAges, Number(v.adultCount || 0)).map((val, i) => (
                    <input
                      key={`ha-${i}`}
                      type="number"
                      className="form-input"
                      min={18}
                      max={65}
                      value={val}
                      onChange={(e) =>
                        onChange((p) => {
                          const arr = ensureLength(p.adultAges, Number(p.adultCount || 0));
                          arr[i] = clampInt(e.target.value, 18, 65);
                          return { ...p, adultAges: arr };
                        })
                      }
                    />
                  ))}
                </div>
              </div>
            )}
            {Number(v.childCount || 0) > 0 && (
              <div>
                <label className="form-label">Child ages</label>
                <div className="plan-field-inline-2">
                  {ensureLength(v.childAges, Number(v.childCount || 0)).map((val, i) => (
                    <input
                      key={`hc-${i}`}
                      type="number"
                      className="form-input"
                      min={0}
                      max={25}
                      value={val}
                      onChange={(e) =>
                        onChange((p) => {
                          const arr = ensureLength(p.childAges, Number(p.childCount || 0));
                          arr[i] = clampInt(e.target.value, 0, 25);
                          return { ...p, childAges: arr };
                        })
                      }
                    />
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
