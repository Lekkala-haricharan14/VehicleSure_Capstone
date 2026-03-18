import os
import re

# Root of the Angular app
ROOT_DIR = r"c:\Users\91814\OneDrive\Desktop\vehicle insurance\frontend\src\app"

# Mapping of symbols to their new locations (relative to src/app)
# This handles the most common shared/core/feature pieces
MAPPING = {
    # Core
    "authGuard": "core/guards/auth.guard",
    "adminGuard": "core/guards/auth.guard",
    "underwriterGuard": "core/guards/auth.guard",
    "guestGuard": "core/guards/auth.guard",
    "jwtInterceptor": "core/interceptors/jwt.interceptor",
    "AuthService": "core/services/auth.service",
    "User": "core/models/user.model",
    # Shared
    "Policy": "shared/models/policy.model",
    "Vehicle": "shared/models/vehicle.model",
    # Features
    "AdminService": "features/admin/services/admin.service",
    "CustomerService": "features/customer/services/customer.service",
    "UnderwriterService": "features/underwriter/services/underwriter.service",
}

# Also handle relative imports that point to deleted directories or changed depths
# We can use a more general approach: if an import points to "services/", "models/", "guards/", "interceptors/"
# we redirect to the new "core/" or "shared/" or "features/" locations.

def get_relative_path(from_dir, to_file_rel_to_app):
    # from_dir is absolute path to the directory containing the file
    # to_file_rel_to_app is something like "core/services/auth.service"
    
    # Target absolute path (without extension)
    target_abs = os.path.join(ROOT_DIR, to_file_rel_to_app).replace("/", os.sep).replace("\\", os.sep)
    
    # Calculate common prefix
    rel = os.path.relpath(target_abs, from_dir).replace(os.sep, "/")
    if not rel.startswith("."):
        rel = "./" + rel
    return rel

def refactor_file(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    modified = False
    lines = content.splitlines()
    new_lines = []

    file_dir = os.path.dirname(file_path)

    for line in lines:
        # Match import { ... } from '...';
        match = re.search(r"import\s+\{(.+?)\}\s+from\s+['\"](.+?)['\"];?", line)
        if match:
            symbols_str, path = match.groups()
            symbols = [s.strip() for s in symbols_str.split(",")]
            
            # Check if any symbol is in our mapping
            found_target = None
            for s in symbols:
                if s in MAPPING:
                    found_target = MAPPING[s]
                    break
            
            if found_target:
                new_rel_path = get_relative_path(file_dir, found_target)
                # Only update if the path is different
                if path != new_rel_path:
                    line = line.replace(path, new_rel_path)
                    modified = True
        
        # Also handle specific relative path rewrites for those not covered by symbols
        # e.g. from '../../components/X' to '../../features/X/components/X'
        # This is trickier, but let's try some common ones
        
        # Rewrite './components/' to './features/...' or similar
        # But most components are now in features/XXX/components/
        
        new_lines.append(line)

    if modified:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write("\n".join(new_lines) + "\n")
        return True
    return False

def main():
    count = 0
    for root, dirs, files in os.walk(ROOT_DIR):
        for file in files:
            if file.endswith(".ts"):
                if refactor_file(os.path.join(root, file)):
                    count += 1
    print(f"Refactored {count} files.")

if __name__ == "__main__":
    main()
