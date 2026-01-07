# Grading Rubric: Problem 01 - Basic Recipe

## Total Points: 100

---

## 1. Recipe Compiles Without Errors (30 points)

### Excellent (27-30 points)
- [ ] Recipe parses successfully (`bitbake -p`)
- [ ] do_fetch completes successfully
- [ ] do_compile completes without warnings
- [ ] do_install completes successfully
- [ ] Full build succeeds (`bitbake hello`)
- [ ] Binary is executable and runs correctly

### Good (21-26 points)
- [ ] Recipe compiles with minor warnings
- [ ] All tasks complete successfully
- [ ] Binary builds and runs
- [ ] Minor issues that don't prevent build

### Satisfactory (15-20 points)
- [ ] Recipe compiles with multiple warnings
- [ ] Some tasks need manual intervention
- [ ] Binary builds but may have issues

### Needs Improvement (0-14 points)
- [ ] Recipe fails to parse
- [ ] Compilation errors
- [ ] Build does not complete
- [ ] Binary doesn't build or run

**Deductions:**
- Parse errors: -10 points
- Compilation failures: -15 points
- Installation failures: -10 points
- Non-functional binary: -5 points

---

## 2. Proper Metadata (20 points)

### Excellent (18-20 points)
- [ ] SUMMARY present and descriptive
- [ ] DESCRIPTION present and detailed
- [ ] LICENSE correctly specified (MIT)
- [ ] LIC_FILES_CHKSUM correct and matches file
- [ ] Optional fields included (HOMEPAGE, SECTION, etc.)

### Good (14-17 points)
- [ ] All required fields present
- [ ] LICENSE and checksum correct
- [ ] Descriptions adequate
- [ ] Some optional fields missing

### Satisfactory (10-13 points)
- [ ] Required fields present
- [ ] Minor errors in metadata
- [ ] Minimal descriptions

### Needs Improvement (0-9 points)
- [ ] Missing required fields
- [ ] Incorrect LICENSE
- [ ] Wrong or missing LIC_FILES_CHKSUM
- [ ] No descriptions

**Deductions:**
- Missing LICENSE: -8 points
- Wrong LIC_FILES_CHKSUM: -5 points
- Missing SUMMARY: -3 points
- Missing DESCRIPTION: -2 points
- Poor quality descriptions: -2 points

---

## 3. Correct Installation Path (20 points)

### Excellent (18-20 points)
- [ ] Binary installed to ${D}${bindir}
- [ ] Correct use of install command
- [ ] Proper permissions set (0755)
- [ ] Directory created before installing
- [ ] No hardcoded paths

### Good (14-17 points)
- [ ] Correct installation location
- [ ] Proper permissions
- [ ] Minor issues with directory creation
- [ ] Minimal hardcoded paths

### Satisfactory (10-13 points)
- [ ] Binary installed but with issues
- [ ] Some hardcoded paths
- [ ] Permission issues

### Needs Improvement (0-9 points)
- [ ] Wrong installation directory
- [ ] Hardcoded /usr/bin instead of ${bindir}
- [ ] Missing ${D} prefix
- [ ] Used cp instead of install
- [ ] Wrong permissions

**Deductions:**
- Not using ${D}: -10 points
- Hardcoding /usr/bin: -5 points
- Using cp instead of install: -3 points
- Wrong permissions: -2 points
- Not creating directory first: -2 points

---

## 4. Follows BitBake Conventions (15 points)

### Excellent (14-15 points)
- [ ] Uses ${CC} for compiler
- [ ] Uses ${CFLAGS} and ${LDFLAGS}
- [ ] Correct use of ${S} and ${WORKDIR}
- [ ] Proper SRC_URI format
- [ ] Standard directory structure
- [ ] Clean task definitions

### Good (11-13 points)
- [ ] Most conventions followed
- [ ] Minor deviations from standards
- [ ] Correct variable usage
- [ ] Mostly standard structure

### Satisfactory (8-10 points)
- [ ] Some conventions followed
- [ ] Several standard variables used
- [ ] Structure mostly correct

### Needs Improvement (0-7 points)
- [ ] Hardcoded gcc instead of ${CC}
- [ ] Doesn't use standard variables
- [ ] Non-standard structure
- [ ] Poor task definitions

**Deductions:**
- Using gcc instead of ${CC}: -7 points
- Not using ${CFLAGS}/${LDFLAGS}: -3 points
- Wrong S variable: -2 points
- Non-standard SRC_URI: -2 points
- Poor directory structure: -2 points

---

## 5. Clean and Readable Code (10 points)

### Excellent (9-10 points)
- [ ] Well-commented code
- [ ] Clear variable names
- [ ] Logical organization
- [ ] Consistent formatting
- [ ] Explains non-obvious choices

### Good (7-8 points)
- [ ] Adequate comments
- [ ] Clear structure
- [ ] Good formatting
- [ ] Some explanation of choices

### Satisfactory (5-6 points)
- [ ] Minimal comments
- [ ] Readable but not polished
- [ ] Basic formatting
- [ ] Limited explanation

### Needs Improvement (0-4 points)
- [ ] No comments
- [ ] Unclear structure
- [ ] Poor formatting
- [ ] Confusing code

**Deductions:**
- No comments: -3 points
- Poor formatting: -2 points
- Unclear structure: -2 points
- Confusing variable names: -2 points
- No explanation of choices: -1 point

---

## 6. Proper File Organization (5 points)

### Excellent (5 points)
- [ ] Correct directory structure
- [ ] Files in proper locations
- [ ] Recipe named correctly (hello_1.0.bb)
- [ ] files/ subdirectory used
- [ ] All source files present

### Good (4 points)
- [ ] Structure mostly correct
- [ ] Minor organizational issues
- [ ] All required files present

### Satisfactory (3 points)
- [ ] Basic structure present
- [ ] Some files in wrong locations
- [ ] Recipe naming correct

### Needs Improvement (0-2 points)
- [ ] Incorrect directory structure
- [ ] Files in wrong locations
- [ ] Missing files
- [ ] Wrong recipe name

**Deductions:**
- Wrong directory structure: -2 points
- Missing files/ directory: -1 point
- Wrong recipe name: -1 point
- Missing source files: -1 point

---

## Bonus Points (up to +10)

### Excellence Indicators:
- [ ] Includes extra documentation (+2)
- [ ] Handles edge cases (+2)
- [ ] Includes test verification (+2)
- [ ] Exceptionally clean code (+2)
- [ ] Creative problem solving (+2)

---

## Common Mistakes Checklist

### Critical Errors (Automatic Fail < 50 points):
- [ ] Recipe doesn't build at all
- [ ] Missing LICENSE or LIC_FILES_CHKSUM
- [ ] Binary not installed or non-functional
- [ ] Uses system gcc instead of ${CC}

### Major Issues (-10 to -15 points each):
- [ ] Not using ${D} in do_install
- [ ] Hardcoded paths instead of BitBake variables
- [ ] Wrong installation directory
- [ ] Missing do_compile or do_install

### Minor Issues (-2 to -5 points each):
- [ ] Poor code formatting
- [ ] Minimal or no comments
- [ ] Missing optional metadata
- [ ] Using cp instead of install
- [ ] Not setting proper file permissions

---

## Grading Scale

| Score | Grade | Assessment |
|-------|-------|------------|
| 90-100 | A | Excellent - Production ready |
| 80-89 | B | Good - Minor improvements needed |
| 70-79 | C | Satisfactory - Several issues to address |
| 60-69 | D | Needs Improvement - Major issues present |
| 0-59 | F | Insufficient - Does not meet requirements |

---

## Sample Feedback

### Example 1: Excellent (95/100)

**Strengths:**
- Recipe builds perfectly without errors
- Excellent use of BitBake variables throughout
- Well-commented and explained
- Proper installation with correct paths
- Comprehensive metadata

**Areas for Improvement:**
- Could add HOMEPAGE and SECTION for completeness
- Consider adding BUILD_DEPENDENCIES comment

**Final Grade: A (95/100)**

---

### Example 2: Good (82/100)

**Strengths:**
- Recipe compiles and installs correctly
- Proper use of ${CC} and cross-compilation variables
- Correct installation path
- Adequate metadata

**Areas for Improvement:**
- Missing some comments explaining choices
- Could use more descriptive DESCRIPTION
- File organization could be cleaner
- Used cp instead of install command (-3 points)

**Final Grade: B (82/100)**

---

### Example 3: Needs Improvement (55/100)

**Critical Issues:**
- Used 'gcc' instead of ${CC} (-7 points)
- Hardcoded /usr/bin instead of ${bindir} (-5 points)
- Missing LIC_FILES_CHKSUM (-5 points)
- Not using ${D} in installation (-10 points)

**Other Issues:**
- Poor code formatting (-2 points)
- No comments (-3 points)
- Minimal metadata (-3 points)

**Recommendations:**
1. Review BitBake variable usage
2. Study cross-compilation concepts
3. Add proper license checksums
4. Use install command with ${D} prefix
5. Improve code documentation

**Final Grade: F (55/100)**

---

## Interviewer Notes

### Time Management:
- 0-10 min: Understanding problem, setting up structure
- 10-20 min: Writing recipe, handling metadata
- 20-25 min: Testing and debugging
- 25-30 min: Final review and cleanup

### Red Flags:
- Not using cross-compilation variables (${CC})
- Hardcoding paths
- Missing license information
- Cannot explain choices
- No testing/verification

### Green Flags:
- Clean, well-structured code
- Proper variable usage
- Good documentation
- Tests before submitting
- Explains reasoning

### Discussion Points:
1. Why use ${CC} instead of gcc?
2. What is ${D} and why is it necessary?
3. How would you add runtime dependencies?
4. What if you needed to link against a library?
5. How would you debug build failures?
