# Yocto/Embedded Linux Interview Question Bank

Comprehensive interview preparation materials for Yocto Project and Embedded Linux positions at companies like NVIDIA, Tesla, Qualcomm, and other embedded systems firms.

## Overview

This question bank is designed to assess candidates at various levels from Junior to Staff Engineer positions. It covers theoretical knowledge, practical coding skills, and system design capabilities.

## Structure

```
interview-questions/
├── conceptual/          # Theoretical questions with detailed answers
├── coding/              # Hands-on coding challenges
├── system-design/       # Architecture and design scenarios
└── README.md           # This file
```

## Conceptual Questions

Located in `conceptual/` directory. These test deep understanding of Yocto concepts.

### 01. Build System Fundamentals (15 questions, Junior-Mid level)
- BitBake basics and task execution
- Recipe syntax and variables
- Build directory structure
- Configuration management
- Package management

**Time:** 45-60 minutes for full assessment

### 02. Recipe Development (15 questions, Mid-Senior level)
- Advanced recipe techniques
- Patching and version management
- PACKAGECONFIG
- Debugging build failures
- Optimization strategies

**Time:** 60-90 minutes for full assessment

### 03. Layer Architecture (12 questions, Mid-Senior level)
- BSP layer design
- Layer organization and priorities
- Cross-layer dependencies
- Custom distro creation
- Multi-machine support

**Time:** 45-60 minutes for full assessment

### 04. Kernel Customization (10 questions, Senior level)
- Kernel recipe customization
- Device tree integration
- Out-of-tree modules
- Kernel debugging
- Boot optimization

**Time:** 60 minutes for full assessment

### 05. meta-tegra & Jetson (10 questions, Senior-Staff level)
- NVIDIA Jetson platform specifics
- CUDA integration
- TensorRT deployment
- Hardware acceleration
- Production deployment

**Time:** 60 minutes for full assessment

## Coding Challenges

Located in `coding/` directory. Hands-on problems to assess practical skills.

### Problem 01: Basic Recipe (Junior, 30 min)
Create a simple hello-world recipe from scratch.

**Skills Tested:**
- Recipe structure
- Basic compilation
- Installation tasks
- License management

### Problem 02: bbappend Extension (Mid, 45 min)
Extend an existing recipe using bbappend files.

**Skills Tested:**
- Recipe extension
- Patch application
- PACKAGECONFIG usage
- File organization

### Problem 03: Kernel Module Recipe (Mid-Senior, 60 min)
Create recipe for out-of-tree kernel module.

**Skills Tested:**
- Kernel module compilation
- Module loading
- Dependencies
- Target deployment

### Problem 04: Custom Image (Mid, 45 min)
Build a custom image recipe with specific packages.

**Skills Tested:**
- Image composition
- Package groups
- IMAGE_FEATURES
- Post-install customization

### Problem 05: systemd Service (Mid-Senior, 60 min)
Create recipe with systemd service integration.

**Skills Tested:**
- Service management
- systemd class usage
- Auto-start configuration
- Dependency ordering

### Problem 06: Multi-Package Recipe (Senior, 60 min)
Split recipe into multiple packages.

**Skills Tested:**
- Package splitting
- FILES variable
- Per-package dependencies
- Library versioning

### Problem 07: Debug Failed Build (Senior, 45 min)
Fix a broken recipe (debugging exercise).

**Skills Tested:**
- Debugging methodology
- Log analysis
- devshell usage
- Problem diagnosis

### Problem 08: Device Tree Recipe (Senior, 60 min)
Integrate custom device tree.

**Skills Tested:**
- Device tree compilation
- Kernel integration
- Platform-specific configuration
- Boot process

### Problem 09: CUDA Recipe (Senior-Staff, 90 min)
Create recipe for CUDA application on Jetson.

**Skills Tested:**
- CUDA toolkit integration
- Cross-compilation for GPU
- Runtime dependencies
- Performance optimization

### Problem 10: Production Image (Staff, 120 min)
Build production-ready minimal image.

**Skills Tested:**
- Security hardening
- Size optimization
- License compliance
- Reproducible builds

## System Design Scenarios

Located in `system-design/` directory. Architecture and design discussions.

### Scenario 01: OTA Update System (Senior-Staff, 90 min)
Design over-the-air update system for 10,000 Jetson device fleet.

**Topics:**
- Update mechanism (SWUpdate, Mender, etc.)
- Rollback strategy
- Security considerations
- Bandwidth optimization
- Fleet management

### Scenario 02: Multi-Product BSP (Senior, 60 min)
Design BSP layer supporting 5 product variants.

**Topics:**
- Layer organization
- Machine configurations
- Shared components
- Variant selection
- Maintenance strategy

### Scenario 03: Secure Boot Chain (Staff, 90 min)
Implement complete secure boot solution.

**Topics:**
- Bootloader security
- Kernel signing
- Verified boot
- Key management
- Attack surface reduction

### Scenario 04: Edge AI Deployment (Senior-Staff, 120 min)
Design edge AI inference platform.

**Topics:**
- Model deployment
- Runtime optimization
- Resource management
- Update mechanism
- Monitoring strategy

### Scenario 05: Build Infrastructure (Staff, 90 min)
Design enterprise Yocto CI/CD pipeline.

**Topics:**
- Build server architecture
- Shared state management
- Parallelization strategy
- Artifact storage
- Testing automation

## Usage Guidelines

### For Candidates

1. **Conceptual Questions**: Read each question carefully, provide detailed answers with examples
2. **Coding Challenges**:
   - Read problem.md first
   - Use hints.md only when stuck
   - Check solution only after attempting
   - Review rubric.md for grading criteria
3. **System Design**: Prepare whiteboard-style explanations with diagrams

### For Interviewers

1. **Select Appropriate Questions**: Match difficulty to candidate level
2. **Time Management**: Use suggested time limits
3. **Discussion**: Focus on reasoning, not just answers
4. **Red Flags**: Note areas in rubric indicating weak understanding
5. **Follow-ups**: Ask follow-up questions from each topic

## Assessment Levels

### Junior Engineer (0-2 years)
- **Conceptual**: Build System Fundamentals (50%+ correct)
- **Coding**: Problems 01-02
- **Focus**: Recipe basics, build understanding

### Mid-Level Engineer (2-4 years)
- **Conceptual**: Build System + Recipe Development (60%+ correct)
- **Coding**: Problems 02-05
- **Focus**: Recipe development, debugging, customization

### Senior Engineer (4-7 years)
- **Conceptual**: All conceptual sections (70%+ correct)
- **Coding**: Problems 05-08
- **System Design**: Scenarios 01-03
- **Focus**: Architecture, optimization, complex integration

### Staff Engineer (7+ years)
- **Conceptual**: All sections + deep dives (80%+ correct)
- **Coding**: Problems 08-10
- **System Design**: All scenarios
- **Focus**: System architecture, production deployment, team leadership

## Scoring Rubric

### Overall Assessment

| Score | Level | Recommendation |
|-------|-------|----------------|
| 90-100% | Exceptional | Strong hire, senior/staff level |
| 80-89% | Strong | Hire at appropriate level |
| 70-79% | Good | Hire with some mentorship |
| 60-69% | Adequate | Junior hire or needs development |
| < 60% | Weak | Not recommended |

### Weighted Scoring

- **Conceptual Knowledge**: 40%
- **Coding Ability**: 35%
- **System Design**: 25%

### Quality Indicators

**Excellent Candidate:**
- Explains reasoning clearly
- Provides real-world examples
- Knows trade-offs
- Asks clarifying questions
- Clean, working code
- Good documentation

**Weak Candidate:**
- Memorized answers without understanding
- Cannot explain choices
- No practical experience
- Code doesn't work
- Poor debugging skills
- Cannot discuss trade-offs

## Interview Format Suggestions

### Phone Screen (45 min)
- 3-5 conceptual questions from Build System Fundamentals
- 1 simple coding problem (Problem 01 or 02)

### Technical Interview Round 1 (90 min)
- 8-10 conceptual questions (mixed difficulty)
- 1-2 coding challenges
- Focus on recipe development

### Technical Interview Round 2 (90 min)
- 1 system design scenario
- 1 complex coding challenge
- Deep dive into previous projects

### Onsite Final Round (60 min)
- Advanced conceptual questions
- Architecture discussion
- Team fit assessment

## Preparation Tips

### For Candidates

1. **Build a Real Project**: Create custom layer with recipes
2. **Read Official Docs**: Yocto Project documentation
3. **Practice Problems**: Work through all coding challenges
4. **Study meta-tegra**: If applying for Jetson-related roles
5. **Understand Why**: Don't just memorize, understand reasoning

### Study Plan (4 weeks)

**Week 1**: Build System Fundamentals
- Complete conceptual questions
- Practice Problems 01-02
- Build hello-world image

**Week 2**: Recipe Development
- Complete conceptual questions
- Practice Problems 03-05
- Create custom recipes

**Week 3**: Layer Architecture & Advanced Topics
- Complete conceptual questions
- Practice Problems 06-08
- Design custom layer

**Week 4**: System Design & Integration
- Review all conceptual
- Practice Problems 09-10
- Work through system design scenarios

## Resources

### Official Documentation
- [Yocto Project Documentation](https://docs.yoctoproject.org/)
- [BitBake User Manual](https://docs.yoctoproject.org/bitbake/)
- [OpenEmbedded Layer Index](https://layers.openembedded.org/)

### Example Layers
- [meta-tegra](https://github.com/OE4T/meta-tegra)
- [meta-raspberrypi](https://git.yoctoproject.org/meta-raspberrypi/)
- [meta-openembedded](https://github.com/openembedded/meta-openembedded)

### Learning Materials
- Yocto Project Quick Build
- Embedded Linux Development Using Yocto Projects (book)
- Linux Foundation Yocto Project Training

## Contributing

This question bank is maintained for interview preparation. Suggestions for improvements:

1. Additional questions covering new topics
2. More coding challenges
3. Updated system design scenarios
4. Real-world case studies

## License

These materials are for educational and interview preparation purposes.

## Feedback

For questions or feedback about these materials, please contact the repository maintainer.

---

**Last Updated**: December 2024
**Version**: 1.0
**Target Yocto Release**: Kirkstone (LTS)
