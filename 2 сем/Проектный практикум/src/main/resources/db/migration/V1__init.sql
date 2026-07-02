CREATE TABLE IF NOT EXISTS draws (
  id UUID PRIMARY KEY,
  status TEXT NOT NULL CHECK (status IN ('ACTIVE','COMPLETED')),
  winning_numbers INTEGER[] NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_draws_status ON draws(status);

CREATE TABLE IF NOT EXISTS tickets (
  id UUID PRIMARY KEY,
  draw_id UUID NOT NULL REFERENCES draws(id) ON DELETE CASCADE,
  status TEXT NOT NULL CHECK (status IN ('PENDING','WIN','LOSE')),
  numbers INTEGER[] NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (draw_id, numbers)
);

CREATE INDEX IF NOT EXISTS idx_tickets_draw_id ON tickets(draw_id);
CREATE INDEX IF NOT EXISTS idx_tickets_status ON tickets(status);
