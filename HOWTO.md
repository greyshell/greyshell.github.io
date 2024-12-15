# How to  manage

## editing the blog

- open the `~/Dropbox/grey_blog/greyshell.github.io` folder in `sublime` editor

## start the site at localhost

- navigate to the greyshell.github.io folder
- type `bundle exec jekyll s` and access the site at http://127.0.0.1:4000/
    - alternate: ./tools/run.sh

## how to post

- copy an existing post from `_post` folder as template.
- save the post related assets inside assets folder. create a folder with the same filename as post.
- for pasting the clipboard data use `pngpaste image_name.png`.
- example: `![image_name](assets/post_name.assets/image_name.png)`
- make sure to put extra line before and after the image_link to avoid `ERROR Errno::ECONNRESET`

### others

- tags are the specific context of the `categories`. for example, in the context of the password storage
    - categories: [crypto]
    - tags: [hashing]
    - TAG names should always be lowercase
- `assets` folder contains all images.
    - folder name should be same as the .md file name in `_posts` folder.

## how to publish

- git commit -am "publish post"
- git push
- check the site at https://greyshell.github.io/
