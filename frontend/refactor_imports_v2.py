import os
import re

ROOT_DIR = r"c:\Users\91814\OneDrive\Desktop\vehicle insurance\frontend\src\app"

# Cache for file locations to avoid repeated walks
FILE_CACHE = {}

def build_cache():
    global FILE_CACHE
    FILE_CACHE = {}
    for root, dirs, files in os.walk(ROOT_DIR):
        for file in files:
            if file.endswith(".ts"):
                # Store relative to ROOT_DIR, without extension
                rel_path = os.path.relpath(os.path.join(root, file), ROOT_DIR).replace(os.sep, "/")
                name_no_ext = os.path.splitext(file)[0]
                # If it's a component, the name might be 'login.component'
                if name_no_ext not in FILE_CACHE:
                    FILE_CACHE[name_no_ext] = []
                FILE_CACHE[name_no_ext].append(rel_path.replace(".ts", ""))

def get_new_relative_path(current_file_abs, import_path_rel):
    current_dir_abs = os.path.dirname(current_file_abs)
    
    # Extract the target filename (last part of the path)
    target_name = os.path.basename(import_path_rel)
    
    if target_name in FILE_CACHE:
        # If there are multiple files with same name, we might have a collision, 
        # but in this project it's likely unique for services/models.
        # Prefer the one that matches part of the path if possible.
        candidates = FILE_CACHE[target_name]
        
        target_rel_to_app = candidates[0] # Default to first
        
        # Calculate new relative path
        target_abs = os.path.join(ROOT_DIR, target_rel_to_app).replace("/", os.sep).replace("\\", os.sep)
        new_rel = os.path.relpath(target_abs, current_dir_abs).replace(os.sep, "/")
        if not new_rel.startswith("."):
            new_rel = "./" + new_rel
        return new_rel
    return None

def refactor_file(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    modified = False
    
    # Regex to find all relative imports
    # import ... from './...'; or import ... from '../...';
    pattern = r"(import\s+.*?from\s+['\"])((\.\.?\/)+.*?)(['\"];?)"
    
    def replacer(match):
        nonlocal modified
        prefix, path, _, suffix = match.groups()
        
        # Skip if it's already a library or absolute-ish (shouldn't happen with our regex)
        if not path.startswith("."):
            return match.group(0)
            
        # Try to find where this file is now
        new_path = get_new_relative_path(file_path, path)
        if new_path and new_path != path:
            modified = True
            return prefix + new_path + suffix
        return match.group(0)

    new_content = re.sub(pattern, replacer, content)

    if modified:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        return True
    return False

def main():
    build_cache()
    print(f"Built cache with {len(FILE_CACHE)} unique filenames.")
    
    count = 0
    for root, dirs, files in os.walk(ROOT_DIR):
        for file in files:
            if file.endswith(".ts"):
                if refactor_file(os.path.join(root, file)):
                    count += 1
    print(f"Refactored {count} files.")

if __name__ == "__main__":
    main()
