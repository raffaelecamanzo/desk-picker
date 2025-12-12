import type { ReactElement } from "react";

const sampleCards = [
  {
    title: "Reserve a desk",
    detail: "Pick your office, choose dates, and auto-assign or pick a seat.",
    cta: "Start reservation"
  },
  {
    title: "See occupancy",
    detail: "Today and tomorrow occupancy at a glance for your office.",
    cta: "View dashboard"
  },
  {
    title: "Admin controls",
    detail: "Upload floor maps, manage closures, and mark seats unavailable.",
    cta: "Open admin"
  }
];

function App(): ReactElement {
  return (
    <div className="page">
      <header className="hero">
        <div className="hero__content">
          <p className="eyebrow">Desk Picker</p>
          <h1>Reserve smarter. Keep offices balanced.</h1>
          <p className="lede">
            Sign in with Google to book seats, view occupancy, and manage layouts without
            slowing anyone down.
          </p>
          <div className="actions">
            <button className="primary">Login with Google</button>
            <button className="ghost">Preview dashboards</button>
          </div>
          <p className="microcopy">
            Built for horizontal scalability, WCAG 2.1 AA accessibility, and fast loading
            experiences.
          </p>
        </div>
        <div className="hero__glow" aria-hidden="true" />
      </header>

      <main className="grid">
        {sampleCards.map((card) => (
          <article key={card.title} className="card">
            <h2>{card.title}</h2>
            <p>{card.detail}</p>
            <button className="text-button">{card.cta}</button>
          </article>
        ))}
        <article className="card card--accent">
          <h2>Sample data</h2>
          <dl>
            <div>
              <dt>Upcoming reservations</dt>
              <dd>4 for the next 7 days</dd>
            </div>
            <div>
              <dt>Today&apos;s occupancy</dt>
              <dd>62% in Milan HQ</dd>
            </div>
            <div>
              <dt>Admin notices</dt>
              <dd>Floor 3 closed on Friday</dd>
            </div>
          </dl>
        </article>
      </main>
    </div>
  );
}

export default App;
