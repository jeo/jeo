from docutils import nodes
from os import path

def make_node(rawtext, text, inliner):
  settings = inliner.document.settings

  this_path = path.dirname(settings._source)
  root_path = path.dirname(settings.env.doc2path('index'))

  uri = '../' * len(this_path[len(root_path)+1:].split('/'))
  uri += 'api/%s.html' % text.replace('.', '/')

  # mod, pkg, clz = text.split(':')
  # base_url = 'http://github.com/jdeolive/jeo'
  # pkg = pkg.replace('.', '/')
  # uri = '%s/blob/master/%s/src/main/java/%s/%s.java' % (base_url, mod, pkg, clz)

  ref = nodes.reference('', text.split('.')[-1])
  ref['refuri'] = uri
  return ref
  
def jeoapi_role(name, rawtext, text, lineno, inliner, options={}, content=[]):
  return [make_node(rawtext, text, inliner)], []

def setup(app):
  app.add_role('jeoapi', jeoapi_role)
  return
