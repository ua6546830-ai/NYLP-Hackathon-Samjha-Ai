import os
import zipfile

def create_pitch_deck(output_path="pitch/Samjha_Rx_Pitch_Deck.pptx"):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    slides_data = [
        (
            "Samjha? Rx (سمجھا؟)",
            "AI Prescription Intelligence & Pharmacist Assistance Platform\nMake your prescription make sense • نسخہ سمجھیں، بے فکری سے صحت مند رہیں\n\nFounder: Uzair Ahmad (Pharm.D Candidate, FUUAST Karachi)\nGoogle AI Hackathon 2026"
        ),
        (
            "1. The Healthcare Blind Spot",
            "• Over 60% of medication errors in developing markets occur AFTER leaving the pharmacy.\n• Illegible handwriting and Latin abbreviations (TDS, PC, × 5/7) confuse patients.\n• Language barrier: Prescriptions are written in English shorthand, but patients speak Urdu or Roman Urdu.\n• General AI chatbots dangerously hallucinate doses and guess unclear drug names."
        ),
        (
            "2. The Product: Samjha? Rx",
            "• A patient-first mobile companion on Android Jetpack Compose + Pharmacist Review workspace.\n• Reliable multimodal prescription extraction (CameraX + Gemini Vision OCR).\n• Plain-language instructions in English, Roman Urdu, and native Urdu script (RTL).\n• Visual 24-hour daily timeline grid with meal timing rules (AC/PC) and AlarmManager dose reminders."
        ),
        (
            "3. The Core Innovation: The Uncertainty Gate",
            "• Safety Contract: Never guess unclear handwriting, drug names, or doses.\n• Confidence-gated OCR: If handwriting confidence < 85% or strength/frequency is missing, the field stays flagged.\n• Actionable Schedule Lock: Dosage schedule is locked until confirmed by human in the loop.\n• Direct one-tap escalation to dispensing pharmacist."
        ),
        (
            "4. Multilingual & Cultural Tailoring",
            "• Pakistan-First Context: Pre-mapped for top national brands (Augmentin, Brufen, Glucophage, Lipitor, Disprin, Nexum, Ciproxin).\n• Roman Urdu Phonetic Phrasing: 'Subah aur shaam khaney ke baad lein' — how families actually speak.\n• Urdu Nastaliq Support: Native Right-to-Left Arabic-script typography with comfortable scaling."
        ),
        (
            "5. Deterministic Clinical Safety Rules Engine",
            "• Zero-Hallucination Neuro-symbolic Architecture: LLMs assist with OCR and translation; peer-reviewed rules evaluate safety.\n• Reference Standards: British National Formulary (BNF 84), Pakistan National Formulary (PNF), FDA Drug Safety, WHO AMR.\n• Rule Outcomes: NO_ISSUES_FOUND (never claims 'safe/approved'), FLAG, or UNABLE_TO_CHECK.\n• Covers NSAID stomach protection, Ciprofloxacin chelation, paracetamol toxicity, and antibiotic courses."
        ),
        (
            "6. Pharmacist Review & Referral Route",
            "• Dedicated route for community & hospital pharmacists.\n• Audit trace: Rule IDs, clinical rationale, evidence citations, and action owners.\n• SBAR Prescriber Query Builder: Automatically drafts structured clarification notes for doctors.\n• Digital sign-off recording pharmacist verification."
        ),
        (
            "7. Privacy-by-Design & Security Architecture",
            "• On-device Room Database: Prescription records stored locally in SQLite (`samjha_rx.db`).\n• Cloud Backup Exclusion: Excluded from Android cloud auto-backup to prevent health data leaks.\n• 4-Digit Local PIN Lock: Prevents casual shoulder-surfing on personal devices.\n• Masked Referral Exports: Names, phone numbers, and addresses are stripped before sharing."
        ),
        (
            "8. Production Technology Stack",
            "• Frontend: 100% Kotlin Jetpack Compose, Material Design 3 editorial glassmorphic UI.\n• Camera Engine: Android CameraX (camera2, lifecycle, core, view) with reticle viewfinder.\n• Local Storage: Android Jetpack Room (Entity, DAO, Database) + SharedPreferences.\n• Intelligence: Google Gemini 3.8 Flash / 2.5 Flash via secure REST API with offline fallbacks.\n• Notifications: Android AlarmManager + BroadcastReceiver + NotificationCompat."
        ),
        (
            "9. Traction, Roadmap & The Ask",
            "• Phase 1 (Completed): Full native Android app, CameraX OCR, Room DB, deterministic clinical rules engine, multilingual UI.\n• Phase 2: Pilot deployments with community pharmacy chains in Karachi & telemedicine integrations.\n• Phase 3: Offline on-device Gemini Nano integration for zero-latency rural clinics.\n• The Ask: Clinical pilot partnerships, community pharmacy validation, and seed mentorship."
        )
    ]

    content_types_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="xml" ContentType="application/xml"/>
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
    <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
    <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
    <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
""" + "".join([f'    <Override PartName="/ppt/slides/slide{i+1}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>\n' for i in range(len(slides_data))]) + "</Types>"

    rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
</Relationships>"""

    pres_rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
""" + "".join([f'    <Relationship Id="rId{i+2}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i+1}.xml"/>\n' for i in range(len(slides_data))]) + "</Relationships>"

    pres_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <p:sldMasterIdLst>
        <p:sldMasterId id="2147483648" r:id="rId1"/>
    </p:sldMasterIdLst>
    <p:sldIdLst>
""" + "".join([f'        <p:sldId id="{256+i}" r:id="rId{i+2}"/>\n' for i in range(len(slides_data))]) + """    </p:sldIdLst>
    <p:sldSz cx="12192000" cy="6858000"/>
    <p:notesSz cx="6858000" cy="9144000"/>
</p:presentation>"""

    theme_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Samjha Rx Theme">
    <a:themeElements>
        <a:clrScheme name="SamjhaScheme">
            <a:dk1><a:srgbClr val="0F172A"/></a:dk1>
            <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
            <a:dk2><a:srgbClr val="1E293B"/></a:dk2>
            <a:lt2><a:srgbClr val="F8FAFC"/></a:lt2>
            <a:accent1><a:srgbClr val="4F46E5"/></a:accent1>
            <a:accent2><a:srgbClr val="0D9488"/></a:accent2>
            <a:accent3><a:srgbClr val="D97706"/></a:accent3>
            <a:accent4><a:srgbClr val="E11D48"/></a:accent4>
            <a:accent5><a:srgbClr val="3B82F6"/></a:accent5>
            <a:accent6><a:srgbClr val="10B981"/></a:accent6>
            <a:hlink><a:srgbClr val="4F46E5"/></a:hlink>
            <a:folHlink><a:srgbClr val="3730A3"/></a:folHlink>
        </a:clrScheme>
        <a:fontScheme name="SamjhaFont">
            <a:majorFont><a:latin typeface="Segoe UI"/><a:ea typeface=""/><a:cs typeface=""/></a:majorFont>
            <a:minorFont><a:latin typeface="Segoe UI"/><a:ea typeface=""/><a:cs typeface=""/></a:minorFont>
        </a:fontScheme>
        <a:fmtScheme name="Office">
            <a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst>
            <a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst>
            <a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>
            <a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst>
        </a:fmtScheme>
    </a:themeElements>
</a:theme>"""

    master_rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
</Relationships>"""

    master_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldMaster xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <p:cSld>
        <p:bg><p:bgRef idx="1001"><a:schemeClr val="lt2"/></p:bgRef></p:bg>
        <p:spTree>
            <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
            <p:grpSpPr/>
        </p:spTree>
    </p:cSld>
    <p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
    <p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst>
</p:sldMaster>"""

    layout_rels_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
</Relationships>"""

    layout_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sldLayout xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" type="blank">
    <p:cSld name="Blank">
        <p:spTree>
            <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
            <p:grpSpPr/>
        </p:spTree>
    </p:cSld>
</p:sldLayout>"""

    slide_rel_xml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
</Relationships>"""

    with zipfile.ZipFile(output_path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("[Content_Types].xml", content_types_xml)
        z.writestr("_rels/.rels", rels_xml)
        z.writestr("ppt/_rels/presentation.xml.rels", pres_rels_xml)
        z.writestr("ppt/presentation.xml", pres_xml)
        z.writestr("ppt/theme/theme1.xml", theme_xml)
        z.writestr("ppt/slideMasters/_rels/slideMaster1.xml.rels", master_rels_xml)
        z.writestr("ppt/slideMasters/slideMaster1.xml", master_xml)
        z.writestr("ppt/slideLayouts/_rels/slideLayout1.xml.rels", layout_rels_xml)
        z.writestr("ppt/slideLayouts/slideLayout1.xml", layout_xml)

        for i, (title, body) in enumerate(slides_data):
            lines = [l.strip() for l in body.split("\n") if l.strip()]
            body_xml_lines = []
            for l in lines:
                clean_l = l.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                body_xml_lines.append(f"""
                <a:p>
                    <a:pPr marL="288000" indent="-288000"/>
                    <a:r>
                        <a:rPr lang="en-US" sz="2000">
                            <a:solidFill><a:srgbClr val="1E293B"/></a:solidFill>
                        </a:rPr>
                        <a:t>{clean_l}</a:t>
                    </a:r>
                </a:p>""")
            
            body_p_xml = "".join(body_xml_lines)
            clean_title = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

            slide_xml = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <p:cSld>
        <p:spTree>
            <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
            <p:grpSpPr/>
            
            <!-- Background Card Accent -->
            <p:sp>
                <p:nvSpPr><p:cNvPr id="2" name="HeaderBox"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                <p:spPr>
                    <a:xfrm><a:off x="609600" y="457200"/><a:ext cx="10972800" cy="1143000"/></a:xfrm>
                    <a:prstGeom prst="roundRect"><a:avLst><a:gd name="adj" fmla="val 12000"/></a:avLst></a:prstGeom>
                    <a:solidFill><a:srgbClr val="EEF2FF"/></a:solidFill>
                    <a:ln w="25400"><a:solidFill><a:srgbClr val="4F46E5"/></a:solidFill></a:ln>
                </p:spPr>
                <p:txBody>
                    <a:bodyPr anchor="ctr"/>
                    <a:p>
                        <a:r>
                            <a:rPr lang="en-US" sz="3200" b="1">
                                <a:solidFill><a:srgbClr val="4F46E5"/></a:solidFill>
                            </a:rPr>
                            <a:t>{clean_title}</a:t>
                        </a:r>
                    </a:p>
                </p:txBody>
            </p:sp>

            <!-- Slide Content Body -->
            <p:sp>
                <p:nvSpPr><p:cNvPr id="3" name="ContentBox"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                <p:spPr>
                    <a:xfrm><a:off x="762000" y="1905000"/><a:ext cx="10668000" cy="4381500"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:noFill/>
                </p:spPr>
                <p:txBody>
                    <a:bodyPr anchor="top"/>
                    {body_p_xml}
                </p:txBody>
            </p:sp>

            <!-- Footer -->
            <p:sp>
                <p:nvSpPr><p:cNvPr id="4" name="Footer"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                <p:spPr>
                    <a:xfrm><a:off x="762000" y="6350000"/><a:ext cx="10668000" cy="304800"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:noFill/>
                </p:spPr>
                <p:txBody>
                    <a:bodyPr/>
                    <a:p>
                        <a:r>
                            <a:rPr lang="en-US" sz="1100" i="1">
                                <a:solidFill><a:srgbClr val="64748B"/></a:solidFill>
                            </a:rPr>
                            <a:t>Samjha? Rx (سمجھا؟) • Uzair Ahmad • Confidential Pitch Presentation • Slide {i+1} of {len(slides_data)}</a:t>
                        </a:r>
                    </a:p>
                </p:txBody>
            </p:sp>

        </p:spTree>
    </p:cSld>
</p:sld>"""
            z.writestr(f"ppt/slides/slide{i+1}.xml", slide_xml)
            z.writestr(f"ppt/slides/_rels/slide{i+1}.xml.rels", slide_rel_xml)

    print(f"Successfully generated {output_path} ({len(slides_data)} slides)")

if __name__ == "__main__":
    create_pitch_deck()
