# oodicalc

Calculates statistics from WebOodi course data.

## Usage

### Pre-compiled

Download a .jar or .app package from [GitHub](https://github.com/ahihi/oodicalc/archives/master).

### Source

1. Install [Clojure](http://clojure.org/) 1.2.0 or later.
2. Install [Leiningen](https://github.com/technomancy/leiningen) 1.4.0 or later.
3. Using your preferred shell, navigate to the OodiCalc project directory and execute the command `lein run`.

### Computing stats

1. Go to WebOodi and open the "Completed studies" view.
2. Copy the contents of the course table (not including the headers) and paste it into the "Oodi data" text area in OodiCalc.
3. Click "Compute". The "Statistics" area will now display some stats about your studies.

## License

Copyright (C) 2011 ahihi

Distributed under the Eclipse Public License, the same as Clojure.
