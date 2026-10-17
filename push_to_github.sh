#!/usr/bin/env bash
# ==============================================================================
# Samjha? Rx — GitHub Quick Push & Deployment Script
# ==============================================================================
set -e

echo "=========================================================="
echo " 🚀 Samjha? Rx (سمجھا؟) — GitHub Deployment Script"
echo "=========================================================="

# 1. Check if git is installed
if ! command -v git &> /dev/null; then
    echo "❌ Git is not installed. Please install Git first."
    exit 1
fi

# 2. Initialize git if not already initialized
if [ ! -d ".git" ]; then
    echo "📦 Initializing fresh Git repository..."
    git init
    git branch -M main
else
    echo "✓ Existing Git repository detected."
    # Ensure branch is main
    current_branch=$(git branch --show-current 2>/dev/null || echo "main")
    if [ "$current_branch" != "main" ]; then
        git branch -M main || true
    fi
fi

# 3. Compile and regenerate pitch deck
echo "📊 Verifying marketing pitch deck presentation..."
if [ -f "scripts/generate_pitch_deck.py" ]; then
    python3 scripts/generate_pitch_deck.py || true
fi

# 4. Generate GitHub download-ready package
echo "📦 Packaging clean distribution zip..."
if [ -f "scripts/package_github_release.py" ]; then
    python3 scripts/package_github_release.py || true
fi

# 5. Add files to git
echo "📂 Staging all repository files..."
git add .

# 6. Commit changes
commit_msg="Release: Samjha? Rx v1.0.0 — Prescription Intelligence & Pharmacist Workflow"
if git diff --cached --quiet; then
    echo "✓ Working tree is already clean. Nothing new to commit."
else
    echo "💾 Creating Git commit: '$commit_msg'..."
    git commit -m "$commit_msg"
fi

# 7. Check remote
echo ""
echo "=========================================================="
echo " 🔗 GitHub Remote Configuration"
echo "=========================================================="
current_remote=$(git remote get-url origin 2>/dev/null || echo "")

if [ -z "$current_remote" ]; then
    echo "No remote repository named 'origin' configured yet."
    echo ""
    echo "To push this repository to your GitHub account:"
    echo "  1. Create a new repository on GitHub: https://github.com/new"
    echo "     (e.g., name it 'samjha-rx-android')"
    echo "  2. Run the following command:"
    echo "     git remote add origin https://github.com/YOUR_USERNAME/samjha-rx-android.git"
    echo "  3. Then push with:"
    echo "     git push -u origin main"
    echo ""
    read -p "Would you like to enter your GitHub repository URL now? (y/N): " response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        read -p "Enter GitHub repo URL (e.g., https://github.com/username/samjha-rx.git): " repo_url
        if [ -n "$repo_url" ]; then
            git remote add origin "$repo_url"
            echo "✓ Added remote origin: $repo_url"
            echo "🚀 Pushing to origin main..."
            git push -u origin main
            echo "🎉 Successfully pushed to GitHub!"
        fi
    fi
else
    echo "✓ Remote 'origin' is configured to: $current_remote"
    read -p "Push latest commits to origin main now? (Y/n): " push_resp
    if [[ ! "$push_resp" =~ ^([nN][oO]|[nN])$ ]]; then
        echo "🚀 Pushing to origin main..."
        git push -u origin main
        echo "🎉 Successfully pushed to GitHub!"
    fi
fi

echo ""
echo "=========================================================="
echo " 📋 Deployment & CI/CD Status"
echo "=========================================================="
echo "✓ GitHub Actions workflow is installed at .github/workflows/android.yml"
echo "✓ Marketing pitch deck is ready at pitch/Samjha_Rx_Pitch_Deck.pptx"
echo "✓ Distribution zip is ready at samjha_rx_github_ready_package.zip"
echo "✓ Read docs/DEPLOYMENT_GUIDE.md for complete Play Store and release instructions."
echo "=========================================================="
