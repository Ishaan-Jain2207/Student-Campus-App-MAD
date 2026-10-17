# SVKM's NMIMS Chandigarh — Source Notes, Content Inventory & CSV Parser Policy

> **Verification Date:** October 16, 2026
> **Primary Official Source:** [NMIMS Chandigarh Official Portal](https://chandigarh.nmims.edu/)

---

## 1. Campus Address & Geocoding Attribution

* **Institution Name:** SVKM's NMIMS Chandigarh
* **Official Address:** Plot No. 5, Education City, Opposite Botanical Garden, adjacent to Govt. Senior Model School, Sarangpur, Chandigarh (U.T.) 160014.
* **Source Reference URLs:**
  - Address & Contact: [https://chandigarh.nmims.edu/get-in-touch/](https://chandigarh.nmims.edu/get-in-touch/)
  - Campus Leadership & Programs: [https://chandigarh.nmims.edu/directors-desk/](https://chandigarh.nmims.edu/directors-desk/)
* **Campus Address-Level Coordinates:** Latitude `30.7832533`, Longitude `76.7619824` (Address-level pin for Plot No. 5, Education City, Sarangpur, Chandigarh).
* **Map Display Policy:** Displays a single campus address pin for Plot No. 5, Education City, Sarangpur, Chandigarh. Unverified internal building pins have been removed.

---

## 2. Constituent Schools & Department Mapping

Published by NMIMS Chandigarh official portal:

| Department Code | School Name | Personal Log Advisory Benchmark |
|---|---|---|
| **STME** | School of Technology Management & Engineering | **80.0%** Benchmark |
| **SOC** | School of Commerce | **80.0%** Benchmark |
| **SOL** | School of Law | **70.0%** Benchmark |

---

## 3. Verified Resources & CSV Parser Specification

1. **Resource Topics & Verification:** App-bundled resource file `assets/campus_resources.csv` contains verified campus guide topics directly citing public pages on [https://chandigarh.nmims.edu/](https://chandigarh.nmims.edu/).
2. **CSV Parser Tokenization (`ResourceFileReader.java`):**
   - RFC-compliant CSV line parser handling quoted strings (`"..."`), escaped double quotes (`""`), commas within quotes, empty lines, and UTF-8 encoding.
   - Tested independently in `ResourceFileReaderTest.java`.
3. **Events Feed Policy:** No events screen is shipped. The app has no verified live event provider, so an event list would imply data that is not available. Campus and Resources link to the official NMIMS Chandigarh site instead.
