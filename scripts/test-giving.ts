import assert from "node:assert/strict";
import { NextRequest } from "next/server";
import { POST } from "../app/api/give/checkout/route";

async function main() {
  const savedKey = process.env.STRIPE_SECRET_KEY;
  const savedFetch = globalThis.fetch;
  const request = (body: unknown) => new NextRequest("https://www.prayerkey.com/api/give/checkout", { method: "POST", body: JSON.stringify(body) });
  try {
    delete process.env.STRIPE_SECRET_KEY;
    assert.equal((await POST(request({ amount: 10, frequency: "once" }))).status, 503);
    process.env.STRIPE_SECRET_KEY = "test-only-placeholder";
    globalThis.fetch = async () => { throw new Error("Invalid input must not reach Stripe"); };
    for (const body of [{ amount: 0, frequency: "once" }, { amount: 10001, frequency: "once" }, { amount: 1.1, frequency: "once" }, { amount: 10, frequency: "weekly" }]) {
      assert.equal((await POST(request(body))).status, 400);
    }
    for (const frequency of ["once", "monthly"]) {
      globalThis.fetch = async (_url, init) => {
        const form = init?.body as URLSearchParams;
        assert.equal(form.get("line_items[0][price_data][unit_amount]"), "1000");
        assert.equal(form.get("mode"), frequency === "monthly" ? "subscription" : "payment");
        assert.equal(form.get("line_items[0][price_data][recurring][interval]"), frequency === "monthly" ? "month" : null);
        assert.ok(form.get("success_url")?.includes("/give/result?session_id="));
        return Response.json({ url: "https://checkout.stripe.com/c/pay/test" });
      };
      const response = await POST(request({ amount: 10, frequency }));
      assert.equal(response.status, 200);
    }
    globalThis.fetch = async () => Response.json({ error: { message: "private provider details" } }, { status: 400 });
    const failure = await POST(request({ amount: 10, frequency: "once" }));
    assert.equal(failure.status, 502);
    assert.ok(!(await failure.text()).includes("private provider details"));
    console.log("Giving checks passed: configuration, validation, cents, recurrence, return route, provider errors. No Stripe requests sent.");
  } finally {
    globalThis.fetch = savedFetch;
    if (savedKey === undefined) delete process.env.STRIPE_SECRET_KEY; else process.env.STRIPE_SECRET_KEY = savedKey;
  }
}
main().catch(error => { console.error(error); process.exitCode = 1; });
