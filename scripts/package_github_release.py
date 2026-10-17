#!/usr/bin/env python3
"""
Samjha? Rx — GitHub Ready Distribution Packager
Creates a pristine, clean ZIP archive containing the complete Android project,
documentation, pitch deck PPTX, marketing collateral, and CI/CD workflows,
excluding local build artifacts, Gradle caches, and temporary files.
"""

import os
import zipfile
import sys

def package_project(output_zip="samjha_rx_github_ready_package.zip"):
    root_dir = "."
    exclude_dirs = {
        ".gradle",
        "build",
        ".kotlinc",
        ".build-outputs",
        "__pycache__",
        "node_modules",
        "dist",
        ".idea",
        ".git"
    }
    exclude_files = {
        output_zip,
        ".DS_Store",
        "local.properties",
        ".env"  # Keep .env.example, exclude actual active .env
    }
    
    print(f"📦 Packaging Samjha? Rx project into '{output_zip}'...")
    
    file_count = 0
    total_uncompressed_bytes = 0

    with zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as zipf:
        for dirpath, dirnames, filenames in os.walk(root_dir):
            # Prune excluded directories in-place
            rel_dir = os.path.relpath(dirpath, root_dir)
            path_parts = set(rel_dir.split(os.sep))
            
            # Skip if any component matches excluded directories
            if any(part in exclude_dirs for part in path_parts):
                continue
                
            # Filter out subdirectories in-place so os.walk doesn't traverse them
            dirnames[:] = [d for d in dirnames if d not in exclude_dirs]

            for filename in filenames:
                if filename in exclude_files or filename.endswith(".pyc") or filename.endswith(".hprof"):
                    continue
                if filename == output_zip or filename.endswith(".zip"):
                    continue
                    
                full_path = os.path.join(dirpath, filename)
                rel_path = os.path.relpath(full_path, root_dir)
                
                # Double-check excluded path prefixes (e.g. app/build/)
                if any(excluded in rel_path.split(os.sep) for excluded in exclude_dirs):
                    continue

                zipf.write(full_path, rel_path)
                file_count += 1
                total_uncompressed_bytes += os.path.getsize(full_path)

    zip_size_mb = os.path.getsize(output_zip) / (1024 * 1024)
    raw_size_mb = total_uncompressed_bytes / (1024 * 1024)
    print(f"✓ Created {output_zip} successfully!")
    print(f"  • Files packaged: {file_count}")
    print(f"  • Archive size: {zip_size_mb:.2f} MB (compressed from {raw_size_mb:.2f} MB)")
    print(f"  • Pitch Deck included: pitch/Samjha_Rx_Pitch_Deck.pptx")
    print(f"  • Ready for GitHub upload or distribution.")

if __name__ == "__main__":
    out_path = sys.argv[1] if len(sys.argv) > 1 else "samjha_rx_github_ready_package.zip"
    package_project(out_path)
